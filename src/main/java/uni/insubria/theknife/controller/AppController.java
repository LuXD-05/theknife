package uni.insubria.theknife.controller;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import uni.insubria.theknife.Main;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.service.RestaurantRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppController {

    @FXML
    private TextField cityTextField;

    @FXML
    protected void onLoginButtonClick() throws IOException {
        //TODO validate user and password
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/home.fxml"));
        SessionController.setScene(fxmlLoader);
    }

    @FXML
    protected void onRegisterButtonClick() {}

    @FXML
    protected void onGuestButtonClick() throws IOException {

        String selectedLocation = cityTextField.getText().trim();

        boolean locationExists = RestaurantRepository.loadRestaurants().stream()
            .map(r -> r.getLocation().split(",")[0].trim())
            .distinct()
            .anyMatch(loc -> loc.equalsIgnoreCase(selectedLocation));
        
        if (locationExists) {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/home.fxml"));
            SessionController.setScene(fxmlLoader);
        } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Nessun ristorante trovato");
            alert.setHeaderText(null);
            alert.setContentText("Non sono presenti ristoranti nella location indicata");
            alert.showAndWait();
        }        
    }
}