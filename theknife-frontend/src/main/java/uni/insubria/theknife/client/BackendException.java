/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.client;

/**
 * Raised when a WebSocket request fails (timeout, transport error, serialization).
 * Repositories catch this and translate it into their {@code SERVICE_ERROR} code so
 * the existing controller error handling keeps working.
 */
public class BackendException extends RuntimeException {

    public BackendException(String message) {
        super(message);
    }

    public BackendException(String message, Throwable cause) {
        super(message, cause);
    }
}
