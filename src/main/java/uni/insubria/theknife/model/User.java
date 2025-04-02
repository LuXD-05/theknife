package uni.insubria.theknife.model;



import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)public class User {
    @EqualsAndHashCode.Include
    private String username;
    private String firstName;
    private String lastName;
    private String password;
    private LocalDate birthDate;
    private String city;
    private Role role;
}





