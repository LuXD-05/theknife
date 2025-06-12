package uni.insubria.theknife.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Represents a user in the TheKnife application.
 * <p>
 * This class contains all the information related to a user, including personal details,
 * authentication information, and associated restaurants (for restaurant owners).
 * </p>
 * <p>
 * Users can have different roles (e.g., customer, restaurant owner) which determine
 * their permissions and available actions in the application.
 * </p>
 */
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    /**
     * Default constructor for the User class.
     * <p>
     * Creates a new User instance with default values for all fields.
     * The Lombok @Setter and @Accessors(chain = true) annotations allow for
     * fluent setting of properties after construction.
     * </p>
     */
    public User() {
        // Default constructor - fields will be initialized with default values
        this.restaurants = new HashSet<>();
    }
    /**
     * The unique username that identifies this user.
     * This field is used for equality checks.
     */
    @EqualsAndHashCode.Include
    private String username;

    /**
     * The user's first name.
     */
    private String firstName;

    /**
     * The user's last name.
     */
    private String lastName;

    /**
     * The user's password (stored in encrypted form).
     */
    private String password;

    /**
     * The user's birth date.
     * Serialized and deserialized using Jackson's LocalDate serializers.
     */
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate birthDate;

    /**
     * The city where the user is located.
     */
    private String city;

    /**
     * The role assigned to this user, which determines their permissions.
     * @see Role
     */
    private Role role;

    /**
     * The set of restaurants owned by this user.
     * Only relevant for users with the RISTORATORE role.
     */
    private HashSet<Restaurant> restaurants = new HashSet<>();
}
