/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.mapper;

import uni.insubria.theknife.backend.entity.RestaurantEntity;
import uni.insubria.theknife.common.dto.RestaurantDto;
import uni.insubria.theknife.common.dto.ReviewDto;

import java.util.List;

/**
 * Converts {@link RestaurantEntity} to the wire {@link RestaurantDto}, embedding
 * the supplied reviews.
 */
public final class RestaurantMapper {

    private RestaurantMapper() {
    }

    public static RestaurantDto toDto(RestaurantEntity e, List<ReviewDto> reviews) {
        return new RestaurantDto(
                e.id,
                e.name,
                e.address,
                e.location,
                e.price,
                e.cuisine,
                e.longitude,
                e.latitude,
                e.phone,
                e.michelinUrl,
                e.websiteUrl,
                e.award,
                e.greenStar,
                e.facilities,
                e.description,
                e.ownerUsername,
                reviews
        );
    }

    /** Copies the mutable fields of a restaurant DTO onto an entity (id/owner kept separate). */
    public static void applyEditable(RestaurantEntity e, RestaurantDto dto) {
        e.name = dto.name();
        e.address = dto.address();
        e.location = dto.location();
        e.price = dto.price();
        e.cuisine = dto.cuisine();
        e.longitude = dto.longitude();
        e.latitude = dto.latitude();
        e.phone = dto.phone();
        e.michelinUrl = dto.michelinUrl();
        e.websiteUrl = dto.websiteUrl();
        e.award = dto.award();
        e.greenStar = dto.greenStar();
        e.facilities = dto.facilities();
        e.description = dto.description();
    }
}
