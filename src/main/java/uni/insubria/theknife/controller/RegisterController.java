package uni.insubria.theknife.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.TextFields;
import uni.insubria.theknife.Main;
import uni.insubria.theknife.model.Role;
import uni.insubria.theknife.repository.UserRepository;
import uni.insubria.theknife.service.AlertService;
import uni.insubria.theknife.service.SecurityService;
import uni.insubria.theknife.service.SessionService;
import uni.insubria.theknife.model.User;

import java.io.IOException;

public class RegisterController {
    @FXML
    private void initialize() {
        TextFields.bindAutoCompletion(cityTextField, SessionService.getLocations());
    }

    @FXML
    private TextField usernameTextField, firstNameTextField, lastNameTextField, passwordTextField, cityTextField;
    @FXML
    private DatePicker birthdateTextField;

    @FXML
    protected void onConfirmButtonClick() throws IOException {
        if (usernameTextField.textProperty().get().isEmpty() || firstNameTextField.textProperty().get().isEmpty() ||
                lastNameTextField.textProperty().get().isEmpty() || passwordTextField.textProperty().get().isEmpty() ||
                cityTextField.textProperty().get().isEmpty() || birthdateTextField.getValue() == null
        ) {
            AlertService.alert(AlertType.WARNING, "ATTENZIONE", "Compilare tutti i campi", null);
            return;
        }
        User user = new User().setUsername(usernameTextField.getText())
                .setFirstName(firstNameTextField.getText())
                .setLastName(lastNameTextField.getText())
                .setPassword(SecurityService.encode(passwordTextField.getText()))
                .setBirthDate(birthdateTextField.getValue())
                .setCity(cityTextField.getText())
                .setRole(Role.CLIENTE);

        UserRepository.ERROR_CODE errorCode = UserRepository.addUser(user);
        switch (errorCode) {
            case DUPLICATED ->
                    AlertService.alert(AlertType.WARNING, "ATTENZIONE", null, "Esiste già un utente con questo username");
            case SERVICE_ERROR -> AlertService.alert(AlertType.ERROR, "SERVICE ERROR", null, "Impossibile inserire utente.");
            default -> {
                FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/home.fxml"));
                SessionService.setScene(fxmlLoader);
            }
        }
    }

}