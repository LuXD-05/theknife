package uni.insubria.theknife.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SecurityService {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String encode(String string) {
        return encoder.encode(string);
    }

    public static boolean validate(String plainString, String hashedString) {
        return encoder.matches(plainString, hashedString);
    }
}
