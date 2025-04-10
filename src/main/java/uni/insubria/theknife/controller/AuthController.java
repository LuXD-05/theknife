package uni.insubria.theknife.controller;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthController {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 
     * @param string
     * @return
     */
    public static String hash(String string) {
        return encoder.encode(string);
    }

    /**
     * 
     * @param plainString
     * @param hashedString
     * @return
     */
    public static boolean verify(String plainString, String hashedString) {
        return encoder.matches(plainString, hashedString);
    }

}
