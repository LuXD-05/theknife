package uni.insubria.theknife.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.textfield.TextFields;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.service.SessionService;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HomeController {
    @FXML
    public TextField selectedLocation;
    @FXML
    private ListView<Restaurant> restaurantListView;

    public void displayRestaurants() {
        Restaurant.Coordinate restaurantCoordinates = SessionService.getRestaurants().stream()
                .filter(restaurant -> restaurant.getLocation().equals(SessionService.getLocation()))
                .map(restaurant -> new Restaurant.Coordinate(restaurant.getLongitude(), restaurant.getLatitude())).findFirst().orElse(null);
        ObservableList<Restaurant> restaurantList = FXCollections.observableArrayList(SessionService.getRestaurants()
                .stream().peek(restaurant -> {
                    if (restaurantCoordinates != null) {
                        double a = calculateDistance(restaurant, restaurantCoordinates);
                        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                        double distance = Math.round(6371 * c); // convert to kilometers
                        restaurant.setDistance(distance);
                    }
                })
                .filter(restaurant -> restaurant.getDistance() < 50) //exclude restaurants far more than 50km
                .sorted(Comparator.comparingDouble(Restaurant::getDistance)) // sort by distance in ascending order
                .collect(Collectors.toList()));
        restaurantListView.setItems(restaurantList);
        restaurantListView.setCellFactory(lv -> new ListCell<>() {
            protected void updateItem(Restaurant restaurant, boolean empty) {
                super.updateItem(restaurant, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(restaurant.getName() + " - " + restaurant.getLocation() + " - (" + restaurant.getDistance() + "km)");
                }
            }
        });

        // add event listener to handle selection
        restaurantListView.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> handleRestaurantSelection());
    }

    @FXML
    Label welcomeLabel;

    @FXML
    private void initialize() {
        if (SessionService.getLocation() != null) {
            selectedLocation.setText(SessionService.getLocation());
        }
        TextFields.bindAutoCompletion(selectedLocation, SessionService.getLocations());
        displayRestaurants();
    }

    @FXML
    private void handleRestaurantSelection() {
        try {
            Restaurant selectedRestaurant = restaurantListView.getSelectionModel().getSelectedItem();
            SessionService.setRestaurantInSession(selectedRestaurant);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/restaurant.fxml"));
            SessionService.setSceneInSession(fxmlLoader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleCitySelection() {
        String selectedCity = selectedLocation.getText();
        SessionService.setLocation(selectedCity);
        displayRestaurants();
    }

    private static double calculateDistance(Restaurant restaurant, Restaurant.Coordinate restaurantCoordinates) {
        double latDistance = Math.toRadians(restaurant.getLatitude() - restaurantCoordinates.getLatitude());
        double lonDistance = Math.toRadians(restaurant.getLongitude() - restaurantCoordinates.getLongitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(restaurantCoordinates.getLatitude())) * Math.cos(Math.toRadians(restaurant.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        return a;
    }
}
