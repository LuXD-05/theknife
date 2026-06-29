/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.common.dto;

import java.util.List;

/**
 * Wire representation of a restaurant, including its embedded reviews. The owner
 * is referenced by username only (acyclic). This mirrors the in-memory shape the
 * legacy JavaFX client used to load from {@code restaurants.json}, so the client
 * model maps to it one-to-one and the existing filtering logic keeps working.
 *
 * @param id           unique restaurant id (legacy hash for seeded data, UUID for new ones)
 * @param name         restaurant name
 * @param address      physical address
 * @param location     city / locality
 * @param price        price band (e.g. "€€")
 * @param cuisine      cuisine type(s)
 * @param longitude    longitude
 * @param latitude     latitude
 * @param phone        phone number
 * @param michelinUrl  Michelin guide URL
 * @param websiteUrl   official website URL
 * @param award        Michelin award/recognition
 * @param greenStar    number of Michelin green stars
 * @param facilities   facilities and services (used by client-side filters)
 * @param description  description text
 * @param ownerUsername username of the owning RISTORATORE, or null
 * @param reviews      reviews for this restaurant
 */
public record RestaurantDto(
        String id,
        String name,
        String address,
        String location,
        String price,
        String cuisine,
        Double longitude,
        Double latitude,
        String phone,
        String michelinUrl,
        String websiteUrl,
        String award,
        Integer greenStar,
        String facilities,
        String description,
        String ownerUsername,
        List<ReviewDto> reviews
) {
}
