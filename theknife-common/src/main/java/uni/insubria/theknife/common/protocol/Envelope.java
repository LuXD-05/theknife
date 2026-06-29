/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.common.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * The single message format exchanged over the WebSocket. The {@code payload} is
 * an opaque {@link JsonNode} decoded per {@link Action} by the receiver, so this
 * type does not need to know about any DTO.
 *
 * @param type          REQUEST, RESPONSE or EVENT
 * @param action        the operation / event
 * @param correlationId UUID set on REQUEST and echoed on RESPONSE; null on EVENT
 * @param error         outcome code, set on RESPONSE only
 * @param message       optional human-readable detail (e.g. error message)
 * @param payload       action-specific JSON body
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Envelope(
        MessageType type,
        Action action,
        String correlationId,
        ErrorCode error,
        String message,
        JsonNode payload
) {

    /** Builds a client REQUEST. */
    public static Envelope request(Action action, String correlationId, JsonNode payload) {
        return new Envelope(MessageType.REQUEST, action, correlationId, null, null, payload);
    }

    /** Builds a successful RESPONSE echoing the given correlationId. */
    public static Envelope ok(Action action, String correlationId, JsonNode payload) {
        return new Envelope(MessageType.RESPONSE, action, correlationId, ErrorCode.NONE, null, payload);
    }

    /** Builds an error RESPONSE echoing the given correlationId. */
    public static Envelope error(Action action, String correlationId, ErrorCode error, String message) {
        return new Envelope(MessageType.RESPONSE, action, correlationId, error, message, null);
    }

    /** Builds a server-initiated broadcast EVENT. */
    public static Envelope event(Action action, JsonNode payload) {
        return new Envelope(MessageType.EVENT, action, null, null, null, payload);
    }
}
