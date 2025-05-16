package uni.insubria.theknife.controller;

import java.io.IOException;
import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import uni.insubria.theknife.Main;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.service.RestaurantRepository;
import uni.insubria.theknife.service.UserRepository;

public class AppController {

    @FXML
    private TextField cityTextField, usernameTextField, passwordTextField;

    @FXML
    protected void onLoginButtonClick() throws IOException {
        //TODO validate user and password

        UserRepository repo = new UserRepository();
        ArrayList<User> users = repo.loadUsers();

        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        // Finds user in string by username
        User user = users.stream().filter(x -> x.getUsername().equals(username)).findFirst().orElse(null);

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

        boolean locationExists = RestaurantRepository.loadRestaurantsCSV().stream()
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