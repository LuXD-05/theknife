/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.service;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uni.insubria.theknife.client.BackendClient;
import uni.insubria.theknife.client.DtoMapper;
import uni.insubria.theknife.model.FilterOptions;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.Review;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.repository.RestaurantRepository;
import uni.insubria.theknife.common.dto.RestaurantDto;
import uni.insubria.theknife.common.dto.ReviewDto;
import uni.insubria.theknife.common.protocol.Envelope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Central session/state holder for the JavaFX client.
 * <p>
 * Unlike the original file-based version, the restaurant catalog is no longer loaded
 * in a static initializer (which would have triggered a blocking network call at class
 * load). Instead {@link #bootstrap()} is invoked once at startup, after the
 * {@link BackendClient} connects, to load the catalog and derive locations/cuisines.
 * It also subscribes to backend broadcast EVENTs to keep the cache live across clients.
 * </p>
 */
public class SessionService {

    public SessionService() {
        // Not meant to be instantiated
    }

    private static final HashMap<String, Object> session = new HashMap<>();

    private static final String STAGE_KEY = "stage";
    private static final String USER_KEY = "user";
    private static final String LOCATION_KEY = "location";
    private static final String RESTAURANT_KEY = "restaurant";
    private static final String FILTERS_KEY = "filters";

    private static List<Restaurant> cachedRestaurants = null;
    private static List<String> locations = null;
    private static List<String> cuisines = null;

    /** UI refresh callback registered by the currently active controller. */
    private static volatile Runnable onDataChanged = null;

    /**
     * One-time bootstrap: loads the catalog from the backend, derives reference lists,
     * and wires real-time updates. Must be called after {@link BackendClient#connect()}.
     */
    public static void bootstrap() {
        cachedRestaurants = new ArrayList<>(RestaurantRepository.loadRestaurants().values());
        recomputeReferenceLists();
        BackendClient.get().setEventListener(SessionService::applyEvent);
    }

    private static void recomputeReferenceLists() {
        locations = cachedRestaurants.stream()
                .map(Restaurant::getLocation)
                .filter(l -> l != null && !l.isBlank())
                .collect(Collectors.toCollection(java.util.TreeSet::new))
                .stream().toList();
        cuisines = cachedRestaurants.stream()
                .map(Restaurant::getCuisine)
                .filter(c -> c != null && !c.isBlank())
                .collect(Collectors.toCollection(java.util.TreeSet::new))
                .stream().toList();
    }

    public static void setStageInSession(Stage stage, FXMLLoader fxmlLoader) throws IOException {
        session.put(STAGE_KEY, stage);
        setSceneInSession(fxmlLoader);
        stage.show();
    }

    public static void setSceneInSession(FXMLLoader fxmlLoader) throws IOException {
        // The active controller re-registers its own data-changed callback in initialize().
        onDataChanged = null;
        Scene scene = new Scene(fxmlLoader.load(), 1024, 720);
        ((Stage) session.get(STAGE_KEY)).setScene(scene);
    }

    public static User getUserFromSession() {
        return ((User) session.get(USER_KEY));
    }

    public static void setUserInSession(User user) {
        session.put(USER_KEY, user);
    }

    public static Optional<Restaurant> getRestaurantFromSession() {
        Object restaurantObj = session.get(RESTAURANT_KEY);
        if (restaurantObj instanceof Restaurant) {
            return Optional.of((Restaurant) restaurantObj);
        }
        return Optional.empty();
    }

    public static void setRestaurantInSession(Restaurant restaurant) {
        session.put(RESTAURANT_KEY, restaurant);
    }

    public static String getLocation() {
        return (String) session.get(LOCATION_KEY);
    }

    public static void setLocation(String selectedLocation) {
        session.put(LOCATION_KEY, selectedLocation);
    }

    public static void clearUserSession() {
        session.remove(USER_KEY);
        session.remove(RESTAURANT_KEY);
        session.remove(LOCATION_KEY);
    }

    //#region Filters

    public static void setFilters(FilterOptions filters) {
        session.put(FILTERS_KEY, filters);
    }

    public static FilterOptions getFilters() {
        return (FilterOptions) session.get(FILTERS_KEY);
    }

    //#endregion

    //#region Reference data

    public static List<String> getLocations() {
        if (locations == null) {
            getRestaurants();
            recomputeReferenceLists();
        }
        return locations;
    }

    public static List<String> getCuisines() {
        if (cuisines == null) {
            getRestaurants();
            recomputeReferenceLists();
        }
        return cuisines;
    }

    //#endregion

    //#region Cached restaurants

    public static List<Restaurant> getRestaurants() {
        if (cachedRestaurants == null) {
            cachedRestaurants = new ArrayList<>(RestaurantRepository.loadRestaurants().values());
            recomputeReferenceLists();
        }
        return cachedRestaurants;
    }

    public static void setRestaurants(List<Restaurant> restaurants) {
        cachedRestaurants = restaurants;
    }

    public static void clearRestaurants() {
        cachedRestaurants = null;
    }

    //#endregion

    //#region Real-time updates

    /** Registers the active controller's refresh callback (run on the FX thread). */
    public static void setOnDataChanged(Runnable callback) {
        onDataChanged = callback;
    }

    /**
     * Applies a backend broadcast EVENT to the in-memory cache and triggers the active
     * controller's refresh. Always executed on the FX thread for thread-safety.
     */
    public static void applyEvent(Envelope event) {
        Platform.runLater(() -> {
            try {
                applyEventInternal(event);
            } catch (Exception ignored) {
                // Best-effort: a malformed event must not break the UI.
            }
            Runnable cb = onDataChanged;
            if (cb != null) {
                cb.run();
            }
        });
    }

    private static void applyEventInternal(Envelope event) {
        if (cachedRestaurants == null || event.action() == null) {
            return;
        }
        JsonNode payload = event.payload();
        switch (event.action()) {
            case RESTAURANT_ADDED -> {
                RestaurantDto dto = convert(payload, RestaurantDto.class);
                if (dto != null && findRestaurant(dto.id()) == null) {
                    cachedRestaurants.add(DtoMapper.toModel(dto));
                    recomputeReferenceLists();
                }
            }
            case RESTAURANT_EDITED -> {
                RestaurantDto dto = convert(payload, RestaurantDto.class);
                Restaurant existing = dto == null ? null : findRestaurant(dto.id());
                if (existing != null) {
                    applyEditableFields(existing, dto);
                    recomputeReferenceLists();
                }
            }
            case RESTAURANT_DELETED -> {
                String id = payload == null ? null : payload.path("id").asText(null);
                if (id != null) {
                    cachedRestaurants.removeIf(r -> id.equals(r.getId()));
                    recomputeReferenceLists();
                }
            }
            case REVIEW_ADDED -> {
                ReviewDto dto = convert(payload, ReviewDto.class);
                Restaurant r = dto == null ? null : findRestaurant(dto.restaurantId());
                if (r != null && r.getReviews().stream().noneMatch(rv -> rv.getId().equals(dto.id()))) {
                    r.getReviews().add(DtoMapper.toModel(dto, r));
                }
            }
            case REVIEW_EDITED -> {
                ReviewDto dto = convert(payload, ReviewDto.class);
                Restaurant r = dto == null ? null : findRestaurant(dto.restaurantId());
                if (r != null) {
                    r.getReviews().stream()
                            .filter(rv -> rv.getId().equals(dto.id()))
                            .findFirst()
                            .ifPresent(rv -> rv.setContent(dto.content()).setStars(dto.stars()).setAnswer(dto.answer()));
                }
            }
            case REVIEW_DELETED -> {
                ReviewDto dto = convert(payload, ReviewDto.class);
                Restaurant r = dto == null ? null : findRestaurant(dto.restaurantId());
                if (r != null) {
                    r.getReviews().removeIf(rv -> rv.getId().equals(dto.id()));
                }
            }
            default -> {
                // Non-event actions are ignored here.
            }
        }
    }

    private static Restaurant findRestaurant(String id) {
        if (id == null) {
            return null;
        }
        return cachedRestaurants.stream().filter(r -> id.equals(r.getId())).findFirst().orElse(null);
    }

    private static void applyEditableFields(Restaurant target, RestaurantDto dto) {
        target.setName(dto.name())
                .setAddress(dto.address())
                .setLocation(dto.location())
                .setPrice(dto.price())
                .setCuisine(dto.cuisine())
                .setLongitude(dto.longitude() == null ? null : dto.longitude().floatValue())
                .setLatitude(dto.latitude() == null ? null : dto.latitude().floatValue())
                .setPhone(dto.phone())
                .setMichelinUrl(dto.michelinUrl())
                .setWebsiteUrl(dto.websiteUrl())
                .setAward(dto.award())
                .setGreenStar(dto.greenStar())
                .setFacilities(dto.facilities())
                .setDescription(dto.description());
    }

    private static <T> T convert(JsonNode node, Class<T> type) {
        if (node == null) {
            return null;
        }
        return BackendClient.get().mapper().convertValue(node, type);
    }

    //#endregion
}
