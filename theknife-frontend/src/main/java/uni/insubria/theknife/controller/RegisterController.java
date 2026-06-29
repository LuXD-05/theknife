/* 
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import org.controlsfx.control.textfield.TextFields;
import uni.insubria.theknife.Main;
import uni.insubria.theknife.model.Role;
import uni.insubria.theknife.repository.UserRepository;
import uni.insubria.theknife.service.AlertService;
import uni.insubria.theknife.service.SessionService;
import uni.insubria.theknife.model.User;

import java.io.IOException;

/**
 * Controller for the registration screen of the TheKnife application.
 * <p>
 * This controller manages the registration view where new users can:
 * </p>
 * <ul>
 *   <li>Create a new account by providing personal information</li>
 *   <li>Select their city or location</li>
 *   <li>Set their login credentials</li>
 * </ul>
 * <p>
 * The controller validates user input, creates new user accounts, and
 * navigates to the home view upon successful registration. By default,
 * new users are assigned the CLIENTE (customer) role.
 * </p>
 */
public class RegisterController {
    /**
     * Default constructor for the RegisterController class.
     * <p>
     * This constructor is automatically called when the FXML loader creates an instance
     * of this controller. It doesn't perform any initialization; all initialization
     * is done in the initialize() method which is called after FXML loading.
     * </p>
     */
    public RegisterController() {
        // Default constructor required by FXML loader
    }
    /**
     * Text fields for user registration information.
     * usernameTextField - For entering the username
     * firstNameTextField - For entering the user's first name
     * lastNameTextField - For entering the user's last name
     * cityTextField - For entering the user's city
     */
    @FXML
    private TextField usernameTextField, firstNameTextField, lastNameTextField, cityTextField;

    /**
     * Password field for entering the user's password.
     */
    @FXML
    private PasswordField passwordTextField;

    /**
     * Date picker for selecting the user's birth date.
     */
    @FXML
    private DatePicker birthdateTextField;

    /**
     * Radio button to select the user as a client.
     */
    @FXML
    private RadioButton clienteRadioButton;

    /**
     * Radio button to select the user as a restaurateur.
     */
    @FXML
    private RadioButton ristoratoreRadioButton;

    /**
     * ToggleGroup that enforces a single selection between CLIENTE and RISTORATORE roles.
     */
    private final ToggleGroup roleToggleGroup = new ToggleGroup();

    /**
     * Initializes the controller.
     * <p>
     * This method is automatically called after the FXML file has been loaded.
     * It sets up auto-completion for the city text field using available locations
     * and initializes the ToggleGroup for user role selection (CLIENTE / RISTORATORE),
     * ensuring that only one role can be selected at a time.
     * </p>
     */
    @FXML
    private void initialize() {
        TextFields.bindAutoCompletion(cityTextField, SessionService.getLocations());

        clienteRadioButton.setToggleGroup(roleToggleGroup);
        ristoratoreRadioButton.setToggleGroup(roleToggleGroup);
    }

    /**
     * Handles the confirm button click event.
     * <p>
     * This method validates the user input, creates a new user account with the
     * provided information, and navigates to the home view upon successful registration.
     * If any required field is empty, displays an appropriate error message.
     * </p>
     *
     * @throws IOException If an error occurs during navigation to the home view
     */
    @FXML
    protected void onConfirmButtonClick() throws IOException {
        if (usernameTextField.textProperty().get().isEmpty() || firstNameTextField.textProperty().get().isEmpty() ||
                lastNameTextField.textProperty().get().isEmpty() || passwordTextField.textProperty().get().isEmpty() ||
                cityTextField.textProperty().get().isEmpty() || birthdateTextField.getValue() == null ||
                roleToggleGroup.getSelectedToggle() == null
        ) {
            AlertService.alert(AlertType.WARNING, "ATTENZIONE", "Compilare tutti i campi", null);
            return;
        }

        User user = new User().setUsername(usernameTextField.getText())
                .setFirstName(firstNameTextField.getText())
                .setLastName(lastNameTextField.getText())
                // Plaintext: the backend hashes the password (BCrypt) server-side.
                .setPassword(passwordTextField.getText())
                .setBirthDate(birthdateTextField.getValue())
                .setCity(cityTextField.getText())
                .setRole(roleToggleGroup.getSelectedToggle() == clienteRadioButton
                    ? Role.CLIENTE
                    : Role.RISTORATORE);

        UserRepository.ERROR_CODE errorCode = UserRepository.addUser(user);
        switch (errorCode) {
            case DUPLICATED ->
                    AlertService.alert(AlertType.WARNING, "ATTENZIONE", null, "Esiste già un utente con questo username");
            case SERVICE_ERROR -> AlertService.alert(AlertType.ERROR, "SERVICE ERROR", null, "Impossibile inserire utente.");
            default -> {
                FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/home.fxml"));
                SessionService.setUserInSession(user);
                SessionService.setLocation(cityTextField.getText());
                SessionService.setSceneInSession(fxmlLoader);
            }
        }
    }

}
