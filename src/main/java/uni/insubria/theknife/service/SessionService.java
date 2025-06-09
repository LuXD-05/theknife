package uni.insubria.theknife.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.repository.RestaurantRepository;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SessionService {
    private static final HashMap<String, Object> session = new HashMap<>();
    private static final String STAGE_KEY = "stage";
    private static final String USER_KEY = "user";
    private static final String LOCATION_KEY = "location";
    private static final String RESTAURANT_KEY = "restaurant";

    @Getter
    private static final List<Restaurant> restaurants = RestaurantRepository.loadRestaurantsCSV();

    @Getter
    private static final List<String> locations = restaurants.stream().map(Restaurant::getLocation).collect(Collectors.toSet()).stream().sorted().toList();


    public static void setStageInSession(Stage stage, FXMLLoader fxmlLoader) throws IOException {
        session.put(STAGE_KEY, stage);
        setSceneInSession(fxmlLoader);
        stage.show();
    }

    public static void setSceneInSession(FXMLLoader fxmlLoader) throws IOException {
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        Scene scene = new Scene(fxmlLoader.load(), screenSize.getWidth(), screenSize.getHeight());
        ((Stage) session.get(STAGE_KEY)).setScene(scene);
    }

    public static User getUserFromSession() {
        return ((User) session.get(USER_KEY));
    }

    public static void setUserInSession(User user) {
        session.put(USER_KEY, user);
    }

    public static Optional<Restaurant> getRestaurantFromSession() {
        Object restaurantObj = session.get(RESTAURANT_KEY);
        if (restaurantObj instanceof Restaurant) {
            return Optional.of((Restaurant) restaurantObj);
        }
        return Optional.empty();
    }

    public static void setRestaurantInSession(Restaurant restaurant) {
        session.put(RESTAURANT_KEY, restaurant);
    }

    public static String getLocation() {
        return (String) session.get(LOCATION_KEY);
    }
    public static void setLocation(String selectedLocation) {
        session.put(LOCATION_KEY, selectedLocation);
    }

    public static void clearUserSession() {
        // Clear user data
        session.remove(USER_KEY);
        session.remove(RESTAURANT_KEY);
        session.remove(LOCATION_KEY);
    }
}