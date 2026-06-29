/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.common.dto;

/**
 * Credentials sent by the client for the {@code LOGIN} action. The plaintext
 * password is validated against the stored BCrypt hash on the server; the hash
 * never travels back to the client.
 *
 * @param username the username
 * @param password the plaintext password
 */
public record LoginRequest(String username, String password) {
}
