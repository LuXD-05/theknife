package uni.insubria.theknife;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uni.insubria.theknife.controller.SessionController;

import java.io.IOException;
import java.util.HashMap;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/index.fxml"));
        stage.setTitle("TheKnife");
        SessionController.setStage(stage,fxmlLoader);
    }

    public static void main(String[] args) {
        launch();
    }

}