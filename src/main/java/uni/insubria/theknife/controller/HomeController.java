package uni.insubria.theknife.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.textfield.TextFields;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.service.SessionService;
import uni.insubria.theknife.util.DistanceCalculator;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HomeController {
    private static final int MAX_DISTANCE_KM = 50;
    private static final String RESTAURANT_VIEW_PATH = "/view/restaurant.fxml";
    private static final String LOGIN_VIEW_PATH = "/view/login.fxml";

    @FXML
    private TextField selectedLocation;
    @FXML
    private ListView<Restaurant> restaurantListView;
    @FXML
    private Label welcomeLabel;

    @FXML
    private void initialize() {
        initializeUserState();
        initializeLocationField();
        displayRestaurants();
    }

    private void initializeUserState() {
        User user = SessionService.getUserFromSession();
        welcomeLabel.setText(String.format("Welcome %s!", user != null ? user.getUsername() : "guest"));
    }

    private void initializeLocationField() {
        if (SessionService.getLocation() != null) {
            selectedLocation.setText(SessionService.getLocation());
        }
        TextFields.bindAutoCompletion(selectedLocation, SessionService.getLocations());
    }

    public void displayRestaurants() {
        Restaurant.Coordinate referenceCoordinates = findReferenceCoordinates();
        List<Restaurant> filteredRestaurants = getFilteredRestaurants(referenceCoordinates);
        setupRestaurantListView(filteredRestaurants);
        setupSelectionHandler();
    }

    private Restaurant.Coordinate findReferenceCoordinates() {
        return SessionService.getRestaurants().stream()
                .filter(restaurant -> restaurant.getLocation().equals(SessionService.getLocation()))
                .map(restaurant -> new Restaurant.Coordinate(restaurant.getLongitude(), restaurant.getLatitude()))
                .findFirst()
                .orElse(null);
    }

    private List<Restaurant> getFilteredRestaurants(Restaurant.Coordinate referenceCoordinates) {
        return SessionService.getRestaurants().stream()
                .peek(restaurant -> updateRestaurantDistance(restaurant, referenceCoordinates))
                .filter(restaurant -> restaurant.getDistance() < MAX_DISTANCE_KM)
                .sorted(Comparator.comparingDouble(Restaurant::getDistance))
                .collect(Collectors.toList());
    }

    private void updateRestaurantDistance(Restaurant restaurant, Restaurant.Coordinate referenceCoordinates) {
        if (referenceCoordinates != null) {
            double distance = DistanceCalculator.calculateDistanceInKm(restaurant, referenceCoordinates);
            restaurant.setDistance(distance);
        }
    }

    private void setupRestaurantListView(List<Restaurant> restaurants) {
        ObservableList<Restaurant> restaurantList = FXCollections.observableArrayList(restaurants);
        restaurantListView.setItems(restaurantList);
        restaurantListView.setCellFactory(this::createRestaurantCell);
    }

    private ListCell<Restaurant> createRestaurantCell(ListView<Restaurant> lv) {
        return new ListCell<>() {
            @Override
            protected void updateItem(Restaurant restaurant, boolean empty) {
                super.updateItem(restaurant, empty);
                setText(empty ? null : formatRestaurantText(restaurant));
            }
        };
    }

    private String formatRestaurantText(Restaurant restaurant) {
        return String.format("%s - %s - (%.0fkm)",
                restaurant.getName(),
                restaurant.getLocation(),
                restaurant.getDistance());
    }

    private void setupSelectionHandler() {
        restaurantListView.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> handleRestaurantSelection());
    }

    @FXML
    private void handleRestaurantSelection() {
        try {
            Restaurant selectedRestaurant = restaurantListView.getSelectionModel().getSelectedItem();
            SessionService.setRestaurantInSession(selectedRestaurant);
            navigateToRestaurantView();
        } catch (IOException e) {
            log.error("Error handling restaurant selection", e);
        }
    }

    private void navigateToRestaurantView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(RESTAURANT_VIEW_PATH));
        SessionService.setSceneInSession(fxmlLoader);
    }

    public void handleCitySelection() {
        String selectedCity = selectedLocation.getText();
        SessionService.setLocation(selectedCity);
        displayRestaurants();
    }

    public void handleLogout(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(LOGIN_VIEW_PATH));
            SessionService.setSceneInSession(fxmlLoader);
        } catch (IOException e) {
            log.error("Error during logout", e);
            throw new RuntimeException(e);
        } finally {
            SessionService.clearUserSession();
        }
    }
}