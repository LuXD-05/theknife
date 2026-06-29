/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import uni.insubria.theknife.common.dto.Role;

import java.time.LocalDate;

/**
 * JPA entity mapped to the {@code users} table. Entities are never serialized to
 * the wire; mappers convert them to {@link uni.insubria.theknife.common.dto.UserDto}.
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    public String username;

    @Column(name = "first_name")
    public String firstName;

    @Column(name = "last_name")
    public String lastName;

    /** BCrypt hash. Never leaves the backend. */
    public String password;

    @Column(name = "birth_date")
    public LocalDate birthDate;

    public String city;

    @Enumerated(EnumType.STRING)
    public Role role;
}
