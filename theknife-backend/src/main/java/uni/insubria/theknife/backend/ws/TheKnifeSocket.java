/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uni.insubria.theknife.common.protocol.Envelope;
import uni.insubria.theknife.common.protocol.ErrorCode;

/**
 * The single WebSocket endpoint of TheKnife backend. Every message is an
 * {@link Envelope}; requests are routed by the {@link Dispatcher} and the resulting
 * RESPONSE is returned to the caller, while any broadcast EVENT is pushed to the
 * other connected clients. Runs on a worker thread ({@link Blocking}) because the
 * handlers perform blocking JDBC work.
 */
@WebSocket(path = "/ws")
public class TheKnifeSocket {

    private static final Logger LOG = LoggerFactory.getLogger(TheKnifeSocket.class);

    @Inject
    ConnectionRegistry registry;
    @Inject
    Dispatcher dispatcher;
    @Inject
    Broadcaster broadcaster;
    @Inject
    ObjectMapper mapper;
    @Inject
    WebSocketConnection connection;

    @OnOpen
    public void onOpen() {
        registry.register(connection);
        LOG.debug("Client connected: {}", connection.id());
    }

    @OnClose
    public void onClose() {
        registry.unregister(connection.id());
        LOG.debug("Client disconnected: {}", connection.id());
    }

    @OnTextMessage
    @Blocking
    public String onMessage(String raw) {
        Envelope req;
        try {
            req = mapper.readValue(raw, Envelope.class);
        } catch (Exception e) {
            return serialize(Envelope.error(null, null, ErrorCode.VALIDATION, "Messaggio non valido"));
        }
        ConnectionState state = registry.state(connection.id());
        HandlerResult result = dispatcher.dispatch(req, state);
        if (result.broadcast() != null) {
            broadcaster.broadcast(result.broadcast(), connection.id());
        }
        return serialize(result.response());
    }

    private String serialize(Envelope envelope) {
        try {
            return mapper.writeValueAsString(envelope);
        } catch (Exception e) {
            LOG.error("Failed to serialize response", e);
            return "{\"type\":\"RESPONSE\",\"error\":\"SERVICE_ERROR\"}";
        }
    }
}
