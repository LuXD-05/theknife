/*
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import uni.insubria.theknife.client.BackendClient;
import uni.insubria.theknife.client.BackendUrl;
import uni.insubria.theknife.model.FilterOptions;
import uni.insubria.theknife.service.SessionService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JavaFX application launcher for the TheKnife client.
 * <p>
 * On startup it connects to the backend over WebSocket and bootstraps the session
 * (loads the restaurant catalog and reference lists) before showing the login view.
 * This replaces the former static eager file load and keeps all network I/O out of
 * static initializers.
 * </p>
 */
public class Launcher extends Application {

    public Launcher() {
        // Default constructor required by JavaFX
    }

    /**
     * Connects to the backend, bootstraps the session and shows the login view.
     * If the backend is unreachable, an error dialog is shown and the app exits.
     *
     * @param stage the primary stage
     * @throws IOException if the login view cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Resolve the backend URL: program parameter > system property > ask in the UI.
        // On connection failure we re-ask, so a wrong/expired ngrok URL can be corrected live.
        String candidate = resolveUrlFromArgs();

        while (true) {
            if (candidate == null || candidate.isBlank()) {
                Optional<String> entered = promptForUrl(BackendClient.get().getUrl());
                if (entered.isEmpty()) {   // user cancelled
                    Platform.exit();
                    return;
                }
                candidate = entered.get();
            }

            String url = BackendUrl.normalize(candidate);
            BackendClient.get().setUrl(url);
            try {
                BackendClient.get().connect();
                SessionService.bootstrap();
                break;   // connected and bootstrapped
            } catch (Exception e) {
                showConnectionError(url, e);
                candidate = null;   // force the prompt on the next iteration
            }
        }

        SessionService.setFilters(new FilterOptions());
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/login.fxml"));
        stage.setTitle("TheKnife");
        SessionService.setStageInSession(stage, fxmlLoader);
    }

    /**
     * Reads the backend URL from the launch parameters, if provided.
     * Accepts a named parameter ({@code --backend-url=...} or {@code --url=...})
     * or the first positional argument. Falls back to the {@code theknife.backend.url}
     * system property. Returns {@code null} when nothing was supplied.
     */
    private String resolveUrlFromArgs() {
        Parameters params = getParameters();
        if (params != null) {
            Map<String, String> named = params.getNamed();
            String byName = named.getOrDefault("backend-url", named.get("url"));
            if (byName != null && !byName.isBlank()) {
                return byName;
            }
            List<String> raw = params.getUnnamed();
            if (raw != null && !raw.isEmpty() && !raw.get(0).isBlank()) {
                return raw.get(0);
            }
        }
        String prop = System.getProperty("theknife.backend.url");
        return (prop != null && !prop.isBlank()) ? prop : null;
    }

    /** Shows the first-step dialog asking for the server address. */
    private Optional<String> promptForUrl(String suggested) {
        TextInputDialog dialog = new TextInputDialog(suggested);
        dialog.setTitle("TheKnife — Connessione al server");
        dialog.setHeaderText("Inserisci l'indirizzo del server");
        dialog.setContentText("URL del server (es. wss://xxxx.ngrok-free.app):");
        return dialog.showAndWait().map(String::trim).filter(s -> !s.isBlank());
    }

    private void showConnectionError(String url, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("TheKnife");
        alert.setHeaderText("Impossibile connettersi al server");
        alert.setContentText("Indirizzo: " + url + "\n\n" + e.getMessage()
                + "\n\nVerifica che il server (e ngrok) sia attivo e reinserisci l'indirizzo.");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
