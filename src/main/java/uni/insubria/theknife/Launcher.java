package uni.insubria.theknife;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import uni.insubria.theknife.service.SessionService;

import java.io.IOException;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/login.fxml"));
        stage.setTitle("TheKnife");
        SessionService.setStageInSession(stage, fxmlLoader);
    }

    public static void main(String[] args) {
        launch(args);
    }
}