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

/**
 * Registration data sent by the client for the {@code REGISTER} action. The
 * server BCrypt-encodes {@code password} before storing it.
 *
 * @param username  desired username
 * @param firstName first name
 * @param lastName  last name
 * @param password  plaintext password (hashed server-side)
 * @param birthDate date of birth
 * @param city      city of residence
 * @param role      requested role
 */
public record RegisterRequest(
        String username,
        String firstName,
        String lastName,
        String password,
        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        LocalDate birthDate,
        String city,
        Role role
) {
}
