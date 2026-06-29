/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for {@link FavoriteEntity} (username + restaurant id).
 */
public class FavoriteId implements Serializable {

    public String username;
    public String restaurantId;

    public FavoriteId() {
    }

    public FavoriteId(String username, String restaurantId) {
        this.username = username;
        this.restaurantId = restaurantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FavoriteId that)) return false;
        return Objects.equals(username, that.username)
                && Objects.equals(restaurantId, that.restaurantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, restaurantId);
    }
}
