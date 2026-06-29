/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.seed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uni.insubria.theknife.backend.entity.FavoriteEntity;
import uni.insubria.theknife.backend.entity.RestaurantEntity;
import uni.insubria.theknife.backend.entity.ReviewEntity;
import uni.insubria.theknife.backend.entity.UserEntity;
import uni.insubria.theknife.common.dto.Role;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Seeds the database from the legacy data files ({@code michelin_my_maps.csv},
 * {@code users.json}, {@code reviews.json}) the first time the schema is empty.
 * <p>
 * Runs on the JVM so it can reproduce the legacy restaurant id
 * ({@code Objects.hash(name, latitude, longitude)}) exactly, which the existing
 * reviews/favorites reference. Idempotent: it does nothing if restaurants already exist.
 */
public class DataImporter {

    private static final Logger LOG = LoggerFactory.getLogger(DataImporter.class);
    private static final int BATCH = 500;

    @Inject
    EntityManager em;

    @ConfigProperty(name = "theknife.seed.enabled", defaultValue = "true")
    boolean seedEnabled;

    @ConfigProperty(name = "theknife.data.dir", defaultValue = "data")
    String dataDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (!seedEnabled) {
            LOG.info("Data seed disabled (theknife.seed.enabled=false).");
            return;
        }
        long existing = em.createQuery("select count(r) from RestaurantEntity r", Long.class).getSingleResult();
        if (existing > 0) {
            LOG.info("Database already populated ({} restaurants); skipping seed.", existing);
            return;
        }

        Path dir = resolveDataDir();
        if (dir == null) {
            LOG.warn("Seed data directory not found (looked for michelin_my_maps.csv); skipping seed.");
            return;
        }
        LOG.info("Seeding database from {}", dir.toAbsolutePath());

        Set<String> restaurantIds = importRestaurants(dir.resolve("michelin_my_maps.csv"));
        importUsers(dir.resolve("users.json"), restaurantIds);
        importReviews(dir.resolve("reviews.json"), restaurantIds);

        LOG.info("Seed complete: {} restaurants, {} users, {} reviews, {} favorites.",
                count("RestaurantEntity"), count("UserEntity"), count("ReviewEntity"), count("FavoriteEntity"));
    }

    private long count(String entity) {
        return em.createQuery("select count(e) from " + entity + " e", Long.class).getSingleResult();
    }

    private Path resolveDataDir() {
        // Candidati per coprire le diverse working directory:
        // - quarkus:dev    -> cwd = theknife-backend  -> "data"
        // - jar dalla root  -> cwd = repo root         -> "theknife-backend/data"
        for (String candidate : List.of(dataDir, "data", "theknife-backend/data", "../data", "../../data")) {
            Path p = Path.of(candidate);
            if (Files.exists(p.resolve("michelin_my_maps.csv"))) {
                return p;
            }
        }
        return null;
    }

    /** Imports restaurants from the CSV; returns the set of imported ids. */
    private Set<String> importRestaurants(Path csv) {
        Set<String> ids = new HashSet<>();
        int collisions = 0;
        int n = 0;
        try (Reader reader = Files.newBufferedReader(csv, StandardCharsets.UTF_8)) {
            CsvToBean<CsvRestaurant> beans = new CsvToBeanBuilder<CsvRestaurant>(reader)
                    .withType(CsvRestaurant.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            for (CsvRestaurant c : beans) {
                String id = c.legacyId();
                if (!ids.add(id)) {
                    collisions++;
                    continue; // keep first occurrence, like the legacy map-based loader
                }
                RestaurantEntity e = new RestaurantEntity();
                e.id = id;
                e.name = c.name;
                e.address = c.address;
                e.location = c.location;
                e.price = c.price;
                e.cuisine = c.cuisine;
                e.longitude = c.longitude == null ? null : c.longitude.doubleValue();
                e.latitude = c.latitude == null ? null : c.latitude.doubleValue();
                e.phone = c.phone;
                e.michelinUrl = c.michelinUrl;
                e.websiteUrl = c.websiteUrl;
                e.award = c.award;
                e.greenStar = c.greenStar;
                e.facilities = c.facilities;
                e.description = c.description;
                e.ownerUsername = null;
                em.persist(e);
                if (++n % BATCH == 0) {
                    em.flush();
                    em.clear();
                }
            }
            em.flush();
            em.clear();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to import restaurants from " + csv, ex);
        }
        if (collisions > 0) {
            LOG.warn("Skipped {} restaurant id collisions/duplicates during import.", collisions);
        }
        LOG.info("Imported {} restaurants.", ids.size());
        return ids;
    }

    /** Imports users, ownership (RISTORATORE) and favorites (CLIENTE). */
    private void importUsers(Path json, Set<String> restaurantIds) {
        JsonNode root = readTree(json);
        if (root == null) {
            return;
        }
        // Pass 1: users
        for (Iterator<Map.Entry<String, JsonNode>> it = root.fields(); it.hasNext(); ) {
            JsonNode u = it.next().getValue();
            String username = text(u, "username");
            if (username == null || username.isBlank()) {
                continue;
            }
            if (em.find(UserEntity.class, username) != null) {
                continue; // dedupe by inner username (file keys may differ from usernames)
            }
            UserEntity e = new UserEntity();
            e.username = username;
            e.firstName = text(u, "firstName");
            e.lastName = text(u, "lastName");
            e.password = text(u, "password");
            e.birthDate = parseBirthDate(u.get("birthDate"));
            e.city = text(u, "city");
            e.role = parseRole(text(u, "role"));
            em.persist(e);
        }
        em.flush();

        // Pass 2: ownership + favorites (restaurants must already be flushed)
        for (Iterator<Map.Entry<String, JsonNode>> it = root.fields(); it.hasNext(); ) {
            JsonNode u = it.next().getValue();
            String username = text(u, "username");
            if (username == null || username.isBlank()) {
                continue;
            }
            Role role = parseRole(text(u, "role"));
            JsonNode arr = u.get("restaurants");
            if (arr == null || !arr.isArray()) {
                continue;
            }
            Set<String> ownFavoriteSeen = new HashSet<>();
            for (JsonNode r : arr) {
                String rid = text(r, "id");
                if (rid == null || !restaurantIds.contains(rid)) {
                    if (rid != null) {
                        LOG.warn("User {} references unknown restaurant id {}; skipping.", username, rid);
                    }
                    continue;
                }
                if (role == Role.RISTORATORE) {
                    RestaurantEntity re = em.find(RestaurantEntity.class, rid);
                    if (re != null && re.ownerUsername == null) {
                        re.ownerUsername = username; // managed entity, flushed at commit
                    } else if (re != null) {
                        LOG.warn("Restaurant {} already owned by {}; ignoring extra owner {}.",
                                rid, re.ownerUsername, username);
                    }
                } else {
                    if (ownFavoriteSeen.add(rid)) {
                        em.persist(new FavoriteEntity(username, rid));
                    }
                }
            }
        }
        em.flush();
    }

    /** Imports reviews, skipping any with dangling user/restaurant references. */
    private void importReviews(Path json, Set<String> restaurantIds) {
        JsonNode root = readTree(json);
        if (root == null) {
            return;
        }
        for (Iterator<Map.Entry<String, JsonNode>> it = root.fields(); it.hasNext(); ) {
            JsonNode rv = it.next().getValue();
            String id = text(rv, "id");
            String username = rv.path("user").path("username").asText(null);
            String restaurantId = rv.path("restaurant").path("id").asText(null);
            if (id == null || username == null || restaurantId == null) {
                continue;
            }
            if (!restaurantIds.contains(restaurantId) || em.find(UserEntity.class, username) == null) {
                LOG.warn("Review {} references unknown user/restaurant ({}/{}); skipping.",
                        id, username, restaurantId);
                continue;
            }
            ReviewEntity e = new ReviewEntity();
            e.id = id;
            e.restaurantId = restaurantId;
            e.username = username;
            e.content = text(rv, "content");
            e.stars = rv.hasNonNull("stars") ? rv.get("stars").asInt() : null;
            e.answer = text(rv, "answer");
            em.persist(e);
        }
        em.flush();
    }

    // --- helpers ---

    private JsonNode readTree(Path json) {
        if (!Files.exists(json)) {
            LOG.warn("Seed file {} not found; skipping.", json);
            return null;
        }
        try {
            return objectMapper.readTree(json.toFile());
        } catch (Exception ex) {
            LOG.warn("Could not parse {}: {}", json, ex.getMessage());
            return null;
        }
    }

    private static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private static Role parseRole(String role) {
        if (role == null) {
            return Role.CLIENTE;
        }
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException ex) {
            return Role.CLIENTE;
        }
    }

    /** birthDate is stored as a [year, month, day] array in the legacy JSON. */
    private static LocalDate parseBirthDate(JsonNode node) {
        if (node == null || !node.isArray() || node.size() < 3) {
            return null;
        }
        try {
            return LocalDate.of(node.get(0).asInt(), node.get(1).asInt(), node.get(2).asInt());
        } catch (Exception ex) {
            return null;
        }
    }
}
