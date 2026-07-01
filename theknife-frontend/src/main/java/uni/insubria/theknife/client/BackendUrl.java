/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Normalizes user-provided backend addresses into the WebSocket endpoint used by
 * the client. This lets users paste either a local address or an ngrok HTTP URL.
 */
public final class BackendUrl {

    private static final String ENDPOINT_PATH = "/ws";

    private BackendUrl() {
        // Utility class
    }

    /**
     * Converts a backend address to a WebSocket URL.
     * <ul>
     *   <li>{@code https://example.ngrok-free.app} becomes {@code wss://example.ngrok-free.app/ws}</li>
     *   <li>{@code http://localhost:8080} becomes {@code ws://localhost:8080/ws}</li>
     *   <li>{@code localhost:8080} becomes {@code ws://localhost:8080/ws}</li>
     * </ul>
     *
     * @param value address entered by the user or passed as an argument
     * @return normalized WebSocket URL
     */
    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("URL backend mancante");
        }

        String candidate = value.trim();
        if (!hasKnownScheme(candidate)) {
            candidate = "ws://" + candidate;
        }

        URI uri = parse(candidate);
        String scheme = normalizeScheme(uri.getScheme());
        String path = normalizePath(uri.getPath());

        try {
            return new URI(
                    scheme,
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    path,
                    uri.getQuery(),
                    uri.getFragment()
            ).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL backend non valida: " + value, e);
        }
    }

    private static boolean hasKnownScheme(String value) {
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.startsWith("ws://")
                || lower.startsWith("wss://")
                || lower.startsWith("http://")
                || lower.startsWith("https://");
    }

    private static String normalizeScheme(String scheme) {
        if (scheme == null) {
            return "ws";
        }
        return switch (scheme.toLowerCase(Locale.ROOT)) {
            case "https", "wss" -> "wss";
            case "http", "ws" -> "ws";
            default -> throw new IllegalArgumentException("Protocollo backend non supportato: " + scheme);
        };
    }

    private static String normalizePath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return ENDPOINT_PATH;
        }
        return path;
    }

    private static URI parse(String value) {
        try {
            URI uri = new URI(value);
            if (uri.getHost() == null) {
                throw new IllegalArgumentException("Host backend mancante: " + value);
            }
            return uri;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL backend non valida: " + value, e);
        }
    }
}
