/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import uni.insubria.theknife.backend.entity.FavoriteEntity;
import uni.insubria.theknife.backend.entity.FavoriteId;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Panache repository for {@link FavoriteEntity} (composite key username + restaurant id).
 */
@ApplicationScoped
public class FavoriteRepository implements PanacheRepositoryBase<FavoriteEntity, FavoriteId> {

    /** Restaurant ids favorited by a user. */
    public Set<String> favoriteRestaurantIds(String username) {
        return this.<FavoriteEntity>list("username", username).stream()
                .map(f -> f.restaurantId)
                .collect(Collectors.toSet());
    }

    /** All favorite rows for a user. */
    public List<FavoriteEntity> findByUser(String username) {
        return list("username", username);
    }
}
