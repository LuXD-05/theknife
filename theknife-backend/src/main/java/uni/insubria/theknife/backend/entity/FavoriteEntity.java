/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * JPA entity mapped to the {@code favorites} join table. The composite primary key
 * (username, restaurant_id) makes the favorite relationship idempotent and
 * concurrency-safe, replacing the legacy in-memory HashSet.
 */
@Entity
@Table(name = "favorites")
@IdClass(FavoriteId.class)
public class FavoriteEntity {

    @Id
    public String username;

    @Id
    @Column(name = "restaurant_id")
    public String restaurantId;

    public FavoriteEntity() {
    }

    public FavoriteEntity(String username, String restaurantId) {
        this.username = username;
        this.restaurantId = restaurantId;
    }
}
