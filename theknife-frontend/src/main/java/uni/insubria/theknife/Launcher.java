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
import javafx.stage.Stage;
import uni.insubria.theknife.client.BackendClient;
import uni.insubria.theknife.model.FilterOptions;
import uni.insubria.theknife.service.SessionService;

import java.io.IOException;

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
        try {
            BackendClient.get().connect();
            SessionService.bootstrap();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("TheKnife");
            alert.setHeaderText("Impossibile connettersi al server");
            alert.setContentText("Avvia il backend (Quarkus) e PostgreSQL, poi riprova.\n\n" + e.getMessage());
            alert.showAndWait();
            Platform.exit();
            return;
        }

        SessionService.setFilters(new FilterOptions());
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/login.fxml"));
        stage.setTitle("TheKnife");
        SessionService.setStageInSession(stage, fxmlLoader);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
