/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.common.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.time.LocalDate;
import java.util.Set;

/**
 * Wire representation of a user. The password hash is intentionally absent: it
 * never leaves the backend. Favorites are carried as the set of restaurant ids
 * the user has marked, keeping the DTO acyclic.
 *
 * @param username             unique username (identity)
 * @param firstName            first name
 * @param lastName             last name
 * @param birthDate            date of birth
 * @param city                 city of residence
 * @param role                 {@link Role}
 * @param favoriteRestaurantIds ids of the restaurants the user favorited
 */
public record UserDto(
        String username,
        String firstName,
        String lastName,
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate birthDate,
        String city,
        Role role,
        Set<String> favoriteRestaurantIds
) {
}
