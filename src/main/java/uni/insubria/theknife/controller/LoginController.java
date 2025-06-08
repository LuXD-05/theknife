package uni.insubria.theknife.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.TextFields;
import uni.insubria.theknife.Main;
import uni.insubria.theknife.service.AlertService;
import uni.insubria.theknife.service.SecurityService;
import uni.insubria.theknife.service.SessionService;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.repository.UserRepository;

import java.io.IOException;

public class LoginController {
    @FXML
    private void initialize() {
        TextFields.bindAutoCompletion(cityTextField, SessionService.getLocations());
    }

    @FXML
    private TextField cityTextField, usernameTextField, passwordTextField;

    @FXML
    protected void onLoginButtonClick() throws IOException {

        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        User user = UserRepository.getUser(username);

        if (user == null) {
            AlertService.alert(AlertType.WARNING, "ATTENZIONE", null, "Utente non trovato");
            return;
        }

        if (SecurityService.validate(password, user.getPassword())) {
            SessionService.setUserInSession(user);
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/home.fxml"));
            SessionService.setSceneInSession(fxmlLoader);
            return;
        }
        AlertService.alert(AlertType.WARNING, "ATTENZIONE", null, "Password errata");
    }

    @FXML
    protected void onRegisterButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/register.fxml"));
        SessionService.setSceneInSession(fxmlLoader);
    }

    @FXML
    protected void onGuestButtonClick() throws IOException {

        String selectedLocation = cityTextField.getText();

        boolean locationExists = SessionService.getLocations().stream().anyMatch(location -> location.equalsIgnoreCase(selectedLocation));

        if (locationExists) {
            SessionService.setLocation(selectedLocation);
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/home.fxml"));
            SessionService.setSceneInSession(fxmlLoader);
        } else {
            AlertService.alert(AlertType.WARNING, "ATTENZIONE", null, "Nessun ristorante trovato nella location indicata");
        }
    }
}