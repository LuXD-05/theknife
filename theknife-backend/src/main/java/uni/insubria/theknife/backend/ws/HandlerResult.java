/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.ws;

import uni.insubria.theknife.common.protocol.Envelope;

/**
 * Outcome of handling a request: the RESPONSE to send back to the caller, plus an
 * optional EVENT to broadcast to the other clients <em>after</em> the transaction commits.
 *
 * @param response  the RESPONSE envelope (never null)
 * @param broadcast the EVENT envelope to broadcast, or null if none
 */
public record HandlerResult(Envelope response, Envelope broadcast) {

    public static HandlerResult of(Envelope response) {
        return new HandlerResult(response, null);
    }

    public static HandlerResult of(Envelope response, Envelope broadcast) {
        return new HandlerResult(response, broadcast);
    }
}
