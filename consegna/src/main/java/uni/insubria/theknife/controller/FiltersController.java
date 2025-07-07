/* 
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.Review;
import uni.insubria.theknife.model.Role;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.repository.ReviewsRepository;
import uni.insubria.theknife.service.AlertService;
import uni.insubria.theknife.service.SessionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import javafx.geometry.Insets;
import javafx.geometry.Side;
import uni.insubria.theknife.model.FilterOptions;
import uni.insubria.theknife.repository.UserRepository;

import static uni.insubria.theknife.util.Messages.*;

/**
 *
 */
public class FiltersController {
    /**
     * Default constructor for the FiltersController class.
     * <p>
     * This constructor is automatically called when the FXML loader creates an instance
     * of this controller. It doesn't perform any initialization; all initialization
     * is done in the initialize() method which is called after FXML loading.
     * </p>
     */
    public FiltersController() {
        // Default constructor required by FXML loader
    }

    @FXML
    private Label welcomeLabel;

    @FXML
    private TextField cuisineField;

    @FXML
    private TextField locationField;

    @FXML
    private ComboBox<String> priceCombo;

    @FXML
    private ComboBox<String> starsCombo;

    @FXML
    private CheckBox deliveryCheck;

    @FXML
    private CheckBox onlineBookingCheck;

    /**
     * Initializes the controller.
     * <p>
     * This method is automatically called after the FXML file has been loaded.
     * It sets up the review editing components, initializes the user state,
     * sets up the rating selector, and populates the restaurant details.
     * </p>
     */
    @FXML
    public void initialize() {

        FilterOptions filters = SessionService.getFilters();
        User user = SessionService.getUserFromSession();
        welcomeLabel.setText(String.format("Welcome %s!", user != null ? user.getUsername() : "guest"));

        // Bind autocomplete for textfields cuisine & location
        TextFields.bindAutoCompletion(cuisineField, param -> {
            String userText = param.getUserText().toLowerCase();
            return SessionService.getCuisines().stream()
                    .filter(c -> c.toLowerCase().contains(userText))
                    .collect(Collectors.toList());
        });
        TextFields.bindAutoCompletion(locationField, param -> {
            String userText = param.getUserText().toLowerCase();
            return SessionService.getLocations().stream()
                    .filter(l -> l.toLowerCase().contains(userText))
                    .collect(Collectors.toList());
        });

        // Cuisine
        cuisineField.setText(filters.getCuisine() == null ? "" : filters.getCuisine());

        // Location
        locationField.setText(filters.getLocation() == null ? "" : filters.getLocation());

        // Average price $
        priceCombo.setItems(FXCollections.observableArrayList("Qualsiasi", "$", "$$", "$$$", "$$$$"));
        priceCombo.setValue(filters.getPrice() == null ? "Qualsiasi" : filters.getPrice());

        // Stelle
        starsCombo.setItems(FXCollections.observableArrayList("Qualsiasi", "1", "2", "3", "4", "5"));
        starsCombo.setValue(filters.getStars() == null ? "Qualsiasi" : filters.getStars());

        deliveryCheck.setSelected(filters.isDeliveryAvailable());
        onlineBookingCheck.setSelected(filters.isOnlineBookingAvailable());

    }

    /**
     * Validates that a user is logged in.
     * <p>
     * This method retrieves the current user from the session and shows
     * an alert if no user is logged in.
     * </p>
     *
     * @return The User object if a user is logged in, null otherwise
     */
    private User validateUser() {
        User user = SessionService.getUserFromSession();
        if (user == null) {
            showAlert(MISSING_INFO_TITLE, USER_NOT_FOUND_MESSAGE);
        }
        return user;
    }

    /**
     * Validates that a restaurant is selected.
     * <p>
     * This method retrieves the current restaurant from the session and shows
     * an alert if no restaurant is selected.
     * </p>
     *
     * @return The Restaurant object if a restaurant is selected, null otherwise
     */
    private Restaurant validateRestaurant() {
        Restaurant restaurant = SessionService.getRestaurantFromSession().orElse(null);
        if (restaurant == null) {
            showAlert(MISSING_INFO_TITLE, RESTAURANT_NOT_FOUND_MESSAGE);
        }
        return restaurant;
    }

    /**
     * Displays an alert dialog with the specified title and content.
     * <p>
     * This method creates and shows a warning alert dialog with the provided
     * title and content. The header text is set to null for a cleaner appearance.
     * </p>
     *
     * @param title   The title text for the alert dialog
     * @param content The content text for the alert dialog
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Handles the back button click event.
     * <p>
     * This method navigates back to the home view, removing the current restaurant
     * from the session and loading the home screen.
     * </p>
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/home.fxml"));
            SessionService.setSceneInSession(fxmlLoader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the logout button click event.
     * <p>
     * This method logs the user out of the application by:
     * </p>
     * <ul>
     *   <li>Navigating to the login view</li>
     *   <li>Clearing the user session data</li>
     * </ul>
     *
     * @param actionEvent The event that triggered the logout action
     * @throws RuntimeException If an error occurs during navigation to the login view
     */
    @FXML
    private void handleLogout(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            SessionService.setSceneInSession(fxmlLoader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            SessionService.clearUserSession();
        }
    }


    @FXML
    private void handleApplyFilters() throws IOException {

        // No --> location vuota = qualsiasi location
        // // if (locationField.getText().isBlank()) {
        // //     showAlert("Filtro mancante", "La location è obbligatoria.");
        // //     return;
        // // }

        FilterOptions filters = new FilterOptions();

        filters.setCuisine(cuisineField.getText().isBlank() ? null : cuisineField.getText().trim());
        filters.setLocation(locationField.getText().isBlank() ? null : locationField.getText().trim());

        String selectedPrice = priceCombo.getValue();
        if (!"Qualsiasi".equals(selectedPrice)) {
            filters.setPrice(selectedPrice);
        }

        String starsValue = starsCombo.getValue();
        if (!"Qualsiasi".equals(starsValue)) {
            filters.setStars(starsValue);
        }

        filters.setDeliveryAvailable(deliveryCheck.isSelected());
        filters.setOnlineBookingAvailable(onlineBookingCheck.isSelected());

        SessionService.setFilters(filters);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/home.fxml"));
        SessionService.setSceneInSession(fxmlLoader);

    }

    @FXML
    private void handleResetFilters() {
        cuisineField.clear();
        locationField.clear();
        priceCombo.setValue("Qualsiasi");
        starsCombo.setValue("Qualsiasi");
        deliveryCheck.setSelected(false);
        onlineBookingCheck.setSelected(false);
    }

}
