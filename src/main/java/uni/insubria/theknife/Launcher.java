package uni.insubria.theknife;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import uni.insubria.theknife.model.FilterOptions;
import uni.insubria.theknife.service.SessionService;

import java.io.IOException;

/**
 * JavaFX application launcher for the TheKnife application.
 * <p>
 * This class extends the JavaFX Application class and is responsible for
 * initializing and starting the JavaFX application. It sets up the initial
 * stage, loads the login view, and integrates with the SessionService to
 * manage application state.
 * </p>
 */
public class Launcher extends Application {
    /**
     * Default constructor for the Launcher class.
     * <p>
     * This constructor is automatically called when the JavaFX runtime creates an instance
     * of this application class. It doesn't perform any initialization; all initialization
     * is done in the start() method which is called after the application has been launched.
     * </p>
     */
    public Launcher() {
        // Default constructor required by JavaFX
    }

    /**
     * Starts the JavaFX application by initializing the primary stage.
     * <p>
     * This method is called by the JavaFX runtime after the application
     * has been initialized. It loads the login view, sets the application
     * title, and stores the stage in the session service.
     * </p>
     *
     * @param stage The primary stage for this application
     * @throws IOException If an error occurs during loading the FXML
     */
    @Override
    public void start(Stage stage) throws IOException {
        
        SessionService.setFilters(new FilterOptions());
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/login.fxml"));
        stage.setTitle("TheKnife");
        SessionService.setStageInSession(stage, fxmlLoader);

    }

    /**
     * The main entry point for the JavaFX application.
     * <p>
     * This method launches the JavaFX application by calling the
     * Application.launch() method.
     * </p>
     *
     * @param args Command line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}
