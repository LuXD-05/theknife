/* 
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Service for handling security-related operations in the TheKnife application.
 * <p>
 * This class provides methods for password encoding and validation using the
 * BCrypt hashing algorithm, which is a secure one-way hashing function designed
 * for password storage.
 * </p>
 * <p>
 * The service uses Spring Security's BCryptPasswordEncoder to implement
 * industry-standard password security practices.
 * </p>
 */
public class SecurityService {
    /**
     * Default constructor for the SecurityService class.
     * <p>
     * This constructor is not meant to be used directly as this class only provides
     * static methods. The class is not designed to be instantiated.
     * </p>
     */
    public SecurityService() {
        // Default constructor - not meant to be used
    }
    /**
     * BCrypt password encoder instance used for hashing and validating passwords.
     */
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Encodes a plain text string using BCrypt hashing algorithm.
     *
     * @param string The plain text string to encode
     * @return The BCrypt-hashed string
     */
    public static String encode(String string) {
        return encoder.encode(string);
    }

    /**
     * Validates whether a plain text string matches a hashed string.
     *
     * @param plainString  The plain text string to validate
     * @param hashedString The hashed string to compare against
     * @return true if the plain string matches the hashed string, false otherwise
     */
    public static boolean validate(String plainString, String hashedString) {
        return encoder.matches(plainString, hashedString);
    }
}
