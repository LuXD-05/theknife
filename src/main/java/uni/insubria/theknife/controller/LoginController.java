package uni.insubria.theknife.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import org.controlsfx.control.textfield.TextFields;

import uni.insubria.theknife.Main;
import uni.insubria.theknife.service.AlertService;
import uni.insubria.theknife.service.SecurityService;
import uni.insubria.theknife.service.SessionService;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.repository.UserRepository;

import java.io.IOException;

import uni.insubria.theknife.model.FilterOptions;

/**
 * Controller for the login screen of the TheKnife application.
 * <p>
 * This controller manages the login view where users can:
 * </p>
 * <ul>
 *   <li>Log in with existing credentials</li>
 *   <li>Navigate to the registration screen</li>
 *   <li>Continue as a guest user</li>
 * </ul>
 * <p>
 * The controller handles authentication by validating user credentials
 * against the user repository and manages navigation to appropriate views
 * based on login success or failure.
 * </p>
 */
public class LoginController {
    /**
     * Default constructor for the LoginController class.
     * <p>
     * This constructor is automatically called when the FXML loader creates an instance
     * of this controller. It doesn't perform any initialization; all initialization
     * is done in the initialize() method which is called after FXML loading.
     * </p>
     */
    public LoginController() {
        // Default constructor required by FXML loader
    }
    /**
     * Text field for entering the city or location.
     */
    @FXML
    private TextField cityTextField;

    /**
     * Text field for entering the username.
     */
    @FXML
    private TextField usernameTextField;

    /**
     * Password field for entering the user's password.
     */
    @FXML
    private PasswordField passwordTextField;

    /**
     * Initializes the controller.
     * <p>
     * This method is automatically called after the FXML file has been loaded.
     * It sets up auto-completion for the city text field using available locations.
     * </p>
     */
    @FXML
    private void initialize() {
        TextFields.bindAutoCompletion(cityTextField, SessionService.getLocations());
    }

    /**
     * Handles the login button click event.
     * <p>
     * This method validates the user credentials against the user repository.
     * If authentication is successful, it navigates to the home view and sets
     * the user in the session. Otherwise, it displays an appropriate error message.
     * </p>
     *
     * @throws IOException If an error occurs during navigation to the home view
     */
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
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/home.fxml"));
            SessionService.setUserInSession(user);
            SessionService.setLocation(user.getCity());
            SessionService.setSceneInSession(fxmlLoader);
            return;
        }
        AlertService.alert(AlertType.WARNING, "ATTENZIONE", null, "Password errata");
    }

    /**
     * Handles the register button click event.
     * <p>
     * This method navigates to the registration view where new users can create an account.
     * </p>
     *
     * @throws IOException If an error occurs during navigation to the registration view
     */
    @FXML
    protected void onRegisterButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/register.fxml"));
        SessionService.setSceneInSession(fxmlLoader);
    }

    /**
     * Handles the guest button click event.
     * <p>
     * This method allows users to continue as guests by selecting a location.
     * If the location exists, it navigates to the home view with the selected location.
     * Otherwise, it displays an error message.
     * </p>
     *
     * @throws IOException If an error occurs during navigation to the home view
     */
    @FXML
    protected void onGuestButtonClick() throws IOException {
        String selectedLocation = cityTextField.getText();

        boolean locationExists = SessionService.getLocations().stream().anyMatch(location -> location.equalsIgnoreCase(selectedLocation));

        if (selectedLocation == null || selectedLocation.isBlank()) {
            SessionService.setFilters(new FilterOptions());
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/home.fxml"));
            SessionService.setSceneInSession(fxmlLoader);
        } else if (locationExists) {
            // SessionService.setLocation(selectedLocation);
            SessionService.setFilters(new FilterOptions().setLocation(selectedLocation));
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/home.fxml"));
            SessionService.setSceneInSession(fxmlLoader);
        } else {
            AlertService.alert(AlertType.WARNING, "ATTENZIONE", null, "Nessun ristorante trovato nella location indicata");
        }
    }
}
