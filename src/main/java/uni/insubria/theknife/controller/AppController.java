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
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.service.RestaurantRepository;
import uni.insubria.theknife.service.UserRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppController {

    @FXML
    private TextField cityTextField, usernameTextField, passwordTextField;

    @FXML
    protected void onLoginButtonClick() throws IOException {
        //TODO validate user and password

        UserRepository repo = new UserRepository();
        Map<String, User> users = repo.loadUsers();

        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        User user = users.get(username);

        if (user == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("ATTENZIONE");
            alert.setHeaderText(null);
            alert.setContentText("Utente non trovato");
            alert.showAndWait();
            return;
        }

        if (!AuthController.verify(password, user.getPassword())) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("ATTENZIONE");
            alert.setHeaderText(null);
            alert.setContentText("Password errata");
            alert.showAndWait();
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/home.fxml"));
        SessionController.setScene(fxmlLoader);
    }

    @FXML
    protected void onRegisterButtonClick() {
        String hashedPassword = AuthController.hash(passwordTextField.getText());
    }

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
            alert.setTitle("ATTENZIONE");
            alert.setHeaderText(null);
            alert.setContentText("Nessun ristorante trovato nella location indicata");
            alert.showAndWait();
        }        
    }
}