/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uni.insubria.theknife.common.protocol.Action;
import uni.insubria.theknife.common.protocol.Envelope;
import uni.insubria.theknife.common.protocol.MessageType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Single, app-lifetime WebSocket connection to the TheKnife backend.
 * <p>
 * Requests are correlated with their responses via a {@code correlationId}:
 * {@link #sendAndWait(Action, Object)} blocks the caller on a {@link CompletableFuture}
 * keyed by that id until the matching RESPONSE arrives, which lets the (synchronous)
 * repository methods keep their original signatures. Server-initiated EVENTs are routed
 * to the registered event listener instead.
 */
public final class BackendClient {

    /** Default backend URL; override with -Dtheknife.backend.url=... */
    private static final String DEFAULT_URL = "ws://localhost:8080/ws";
    private static final long TIMEOUT_SECONDS = 15;

    private static final BackendClient INSTANCE = new BackendClient();

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Map<String, CompletableFuture<Envelope>> pending = new ConcurrentHashMap<>();

    private volatile WebSocket webSocket;
    private volatile Consumer<Envelope> eventListener;
    private volatile String url;

    private BackendClient() {
    }

    public static BackendClient get() {
        return INSTANCE;
    }

    /**
     * Sets the backend WebSocket URL to use on the next {@link #connect()}.
     * When unset, {@code connect()} falls back to the {@code theknife.backend.url}
     * system property and then to {@link #DEFAULT_URL}.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /** The URL that will be (or was) used to connect. */
    public String getUrl() {
        return url != null ? url : System.getProperty("theknife.backend.url", DEFAULT_URL);
    }

    /** Listener invoked (off the FX thread) when the server pushes a broadcast EVENT. */
    public void setEventListener(Consumer<Envelope> listener) {
        this.eventListener = listener;
    }

    /** Opens the connection, blocking until the handshake completes. */
    public void connect() {
        String target = getUrl();
        try {
            webSocket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    // Skip ngrok's browser-warning interstitial on the WS handshake.
                    .header("ngrok-skip-browser-warning", "true")
                    .buildAsync(URI.create(target), new ClientListener())
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new BackendException("Impossibile connettersi al backend (" + target + ")", e);
        }
    }

    public boolean isConnected() {
        return webSocket != null && !webSocket.isOutputClosed();
    }

    /**
     * Sends a request and blocks until the correlated response arrives.
     *
     * @param action  the action
     * @param payload payload object (serialized to JSON), or null
     * @return the RESPONSE envelope
     * @throws BackendException on timeout / transport / serialization failure
     */
    public Envelope sendAndWait(Action action, Object payload) {
        if (webSocket == null) {
            throw new BackendException("Backend non connesso");
        }
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<Envelope> future = new CompletableFuture<>();
        pending.put(correlationId, future);
        try {
            Envelope request = Envelope.request(action, correlationId,
                    payload == null ? null : mapper.valueToTree(payload));
            webSocket.sendText(mapper.writeValueAsString(request), true);
            return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new BackendException("Richiesta " + action + " fallita: " + e.getMessage(), e);
        } finally {
            pending.remove(correlationId);
        }
    }

    /** Access to the shared, configured ObjectMapper (used by the DTO mapper). */
    public ObjectMapper mapper() {
        return mapper;
    }

    private void handle(String raw) {
        try {
            Envelope env = mapper.readValue(raw, Envelope.class);
            if (env.type() == MessageType.EVENT) {
                Consumer<Envelope> listener = eventListener;
                if (listener != null) {
                    listener.accept(env);
                }
            } else if (env.correlationId() != null) {
                CompletableFuture<Envelope> future = pending.remove(env.correlationId());
                if (future != null) {
                    future.complete(env);
                }
            }
        } catch (Exception e) {
            // Ignore malformed frames; pending requests will time out if unanswered.
        }
    }

    /** Accumulates partial text frames (the catalog payload spans many frames). */
    private final class ClientListener implements WebSocket.Listener {
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            buffer.append(data);
            if (last) {
                String message = buffer.toString();
                buffer.setLength(0);
                handle(message);
            }
            ws.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            failAll(error);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
            failAll(new BackendException("Connessione chiusa dal server"));
            return null;
        }

        private void failAll(Throwable error) {
            pending.values().forEach(f -> f.completeExceptionally(error));
            pending.clear();
        }
    }
}
