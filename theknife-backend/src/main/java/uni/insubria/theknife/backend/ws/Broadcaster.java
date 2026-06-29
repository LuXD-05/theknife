/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.websockets.next.OpenConnections;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uni.insubria.theknife.common.protocol.Envelope;

/**
 * Sends server-initiated EVENT envelopes to connected clients, so that changes made
 * by one client (new review, owner reply, restaurant add/edit/delete) appear in
 * real time on the others. Uses the framework-managed {@link OpenConnections}.
 */
@ApplicationScoped
public class Broadcaster {

    private static final Logger LOG = LoggerFactory.getLogger(Broadcaster.class);

    @Inject
    OpenConnections connections;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Broadcasts an event to every open connection except the originator.
     *
     * @param event              the EVENT envelope
     * @param exceptConnectionId connection id of the client that triggered the change (may be null)
     */
    public void broadcast(Envelope event, String exceptConnectionId) {
        final String json;
        try {
            json = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            LOG.error("Failed to serialize broadcast event {}", event.action(), e);
            return;
        }
        int sent = 0;
        for (WebSocketConnection conn : connections.listAll()) {
            if (conn.id().equals(exceptConnectionId) || !conn.isOpen()) {
                continue;
            }
            try {
                conn.sendTextAndAwait(json);
                sent++;
            } catch (Exception failure) {
                LOG.warn("Failed to push event to {}: {}", conn.id(), failure.getMessage());
            }
        }
        LOG.debug("Broadcast {} to {} client(s)", event.action(), sent);
    }
}
