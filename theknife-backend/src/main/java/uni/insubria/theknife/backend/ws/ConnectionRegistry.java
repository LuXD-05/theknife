/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.backend.ws;

import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the currently open WebSocket connections and their per-connection
 * {@link ConnectionState}. Enables broadcasting and authorization checks across
 * multiple concurrently connected clients.
 */
@ApplicationScoped
public class ConnectionRegistry {

    private final ConcurrentHashMap<String, WebSocketConnection> connections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConnectionState> states = new ConcurrentHashMap<>();

    public void register(WebSocketConnection connection) {
        connections.put(connection.id(), connection);
        states.put(connection.id(), new ConnectionState());
    }

    public void unregister(String connectionId) {
        connections.remove(connectionId);
        states.remove(connectionId);
    }

    /** Per-connection state; never null for an open connection. */
    public ConnectionState state(String connectionId) {
        return states.computeIfAbsent(connectionId, k -> new ConnectionState());
    }

    public Collection<WebSocketConnection> all() {
        return connections.values();
    }
}
