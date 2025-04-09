package uni.insubria.theknife;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import uni.insubria.theknife.controller.SessionController;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/index.fxml"));
        stage.setTitle("TheKnife");
        SessionController.setStage(stage, fxmlLoader);

        TestLoadRestaurants();
        
    }

    public static void main(String[] args) {
        launch();
    }

    /**
     * Test method to see if restaurants are being loaded
     */
    private void TestLoadRestaurants() {
        System.out.println(uni.insubria.theknife.service.RestaurantRepository.loadRestaurants().size() > 0 ? "Ristoranti caricati correttamente" : "Errore caricamento ristoranti");
    }

}