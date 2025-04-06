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

    public User(){} //Richiesto da Jackson

    public User(String username, String firstName, String lastName, String password, LocalDate birthDate, String city, Role role){
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.birthDate = birthDate;
        this.city = city;
        this.role = role;
    }        

}





