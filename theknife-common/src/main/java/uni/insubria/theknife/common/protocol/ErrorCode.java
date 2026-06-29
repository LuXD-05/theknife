/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.common.protocol;

/**
 * Outcome code carried by a RESPONSE {@link Envelope}. It is a superset of the
 * legacy {@code ERROR_CODE} the JavaFX repositories used; the client collapses it
 * back onto those enums so the existing controller branches keep working.
 */
public enum ErrorCode {
    /** Operation completed successfully. */
    NONE,
    /** The entity already exists / a uniqueness constraint was violated. */
    DUPLICATED,
    /** The target entity was not found. */
    NOT_FOUND,
    /** The caller is not authenticated or not allowed to perform the action. */
    UNAUTHORIZED,
    /** The request payload failed validation. */
    VALIDATION,
    /** A server-side error occurred. */
    SERVICE_ERROR
}
