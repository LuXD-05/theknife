package uni.insubria.theknife.model;



import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @EqualsAndHashCode.Include
    private String username;
    private String firstname;
    private String lastname;
    private String password;
    private LocalDate birthdate;
    private String city;
    private Role role;

    public User(){} //Richiesto da Jackson

    public User(String username, String firstname, String lastname, String password, LocalDate birthdate, String city, Role role){
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        this.birthdate = birthdate;
        this.city = city;
        this.role = role;
    }        

}





