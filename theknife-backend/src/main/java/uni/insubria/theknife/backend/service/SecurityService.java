/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt password hashing, server-side only. Uses the same algorithm/cost as the
 * legacy client ({@code $2a$10$...}) so users seeded from the original JSON can log in.
 */
@ApplicationScoped
public class SecurityService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** Hashes a plaintext value. */
    public String encode(String raw) {
        return encoder.encode(raw);
    }

    /** Verifies a plaintext value against a stored BCrypt hash. */
    public boolean validate(String raw, String hash) {
        if (raw == null || hash == null) {
            return false;
        }
        return encoder.matches(raw, hash);
    }
}
