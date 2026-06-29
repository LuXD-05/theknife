/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import uni.insubria.theknife.backend.entity.ReviewEntity;

import java.util.List;

/**
 * Panache repository for {@link ReviewEntity} (primary key: id).
 */
@ApplicationScoped
public class ReviewRepository implements PanacheRepositoryBase<ReviewEntity, String> {

    /** All reviews for a given restaurant. */
    public List<ReviewEntity> findByRestaurant(String restaurantId) {
        return list("restaurantId", restaurantId);
    }

    /** Whether the given user already reviewed the given restaurant. */
    public boolean existsForUserAndRestaurant(String username, String restaurantId) {
        return count("username = ?1 and restaurantId = ?2", username, restaurantId) > 0;
    }
}
