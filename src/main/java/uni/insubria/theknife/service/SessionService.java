package uni.insubria.theknife.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.repository.RestaurantRepository;
import uni.insubria.theknife.model.FilterOptions;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing application session state in the TheKnife application.
 * <p>
 * This class provides methods for storing and retrieving various objects from the session,
 * such as the current user, stage, scene, location, and restaurant. It acts as a central
 * point for managing application state and navigation between different views.
 * </p>
 * <p>
 * The service also provides access to application-wide data such as the list of
 * restaurants and available locations.
 * </p>
 */
public class SessionService {
    /**
     * Default constructor for the SessionService class.
     * <p>
     * This constructor is not meant to be used directly as this class only provides
     * static methods. The class is not designed to be instantiated.
     * </p>
     */
    public SessionService() {
        // Default constructor - not meant to be used
    }
    /**
     * The main session storage map that holds all session data.
     */
    private static final HashMap<String, Object> session = new HashMap<>();

    /**
     * Key for storing the application stage in the session.
     */
    private static final String STAGE_KEY = "stage";

    /**
     * Key for storing the current user in the session.
     */
    private static final String USER_KEY = "user";

    /**
     * Key for storing the current location in the session.
     */
    private static final String LOCATION_KEY = "location";

    /**
     * Key for storing the current restaurant in the session.
     */
    private static final String RESTAURANT_KEY = "restaurant";

    private static final String FILTERS_KEY = "filters";

    /**
     * List of all restaurants available in the application.
     * This is loaded once when the class is initialized.
     */
    @Getter
    private static final List<Restaurant> restaurants = RestaurantRepository.loadRestaurants().values().stream().toList();

    /**
     * List of all unique locations where restaurants are available.
     * This is derived from the restaurant list and sorted alphabetically.
     */
    @Getter
    private static final List<String> locations = restaurants.stream().map(Restaurant::getLocation).collect(Collectors.toSet()).stream().sorted().toList();

    /**
     * List of all unique cuisines where restaurants are available.
     * This is derived from the restaurant list and sorted alphabetically.
     */
    @Getter
    private static final List<String> cuisines = restaurants.stream().map(Restaurant::getCuisine).collect(Collectors.toSet()).stream().sorted().toList();

    //TODO
    //GITHUB TASK #5 add list of cousine, price, facilities, awars, greenstar

    /**
     * Sets the application stage in the session and initializes it with the provided FXML loader.
     *
     * @param stage      The JavaFX Stage to set in the session
     * @param fxmlLoader The FXMLLoader containing the scene to load
     * @throws IOException If an error occurs during loading the FXML
     */
    public static void setStageInSession(Stage stage, FXMLLoader fxmlLoader) throws IOException {
        session.put(STAGE_KEY, stage);
        setSceneInSession(fxmlLoader);
        stage.show();
    }

    /**
     * Sets a new scene in the current stage using the provided FXML loader.
     * <p>
     * This method loads the FXML content, creates a new scene sized to the screen dimensions,
     * and sets it as the current scene in the application stage.
     * </p>
     *
     * @param fxmlLoader The FXMLLoader containing the scene to load
     * @throws IOException If an error occurs during loading the FXML
     */
    public static void setSceneInSession(FXMLLoader fxmlLoader) throws IOException {
        // Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        Scene scene = new Scene(fxmlLoader.load(), 1024, 720);
        ((Stage) session.get(STAGE_KEY)).setScene(scene);
    }

    /**
     * Retrieves the current user from the session.
     *
     * @return The User object representing the current user, or null if no user is logged in
     */
    public static User getUserFromSession() {
        return ((User) session.get(USER_KEY));
    }

    /**
     * Sets the current user in the session.
     *
     * @param user The User object to set as the current user
     */
    public static void setUserInSession(User user) {
        session.put(USER_KEY, user);
    }

    /**
     * Retrieves the current restaurant from the session.
     *
     * @return An Optional containing the Restaurant object if present, or an empty Optional if not
     */
    public static Optional<Restaurant> getRestaurantFromSession() {
        Object restaurantObj = session.get(RESTAURANT_KEY);
        if (restaurantObj instanceof Restaurant) {
            return Optional.of((Restaurant) restaurantObj);
        }
        return Optional.empty();
    }

    /**
     * Sets the current restaurant in the session.
     *
     * @param restaurant The Restaurant object to set as the current restaurant
     */
    public static void setRestaurantInSession(Restaurant restaurant) {
        session.put(RESTAURANT_KEY, restaurant);
    }

    /**
     * Retrieves the current location from the session.
     *
     * @return The current location as a String, or null if no location is set
     */
    public static String getLocation() {
        return (String) session.get(LOCATION_KEY);
    }

    /**
     * Sets the current location in the session.
     *
     * @param selectedLocation The location to set as the current location
     */
    public static void setLocation(String selectedLocation) {
        session.put(LOCATION_KEY, selectedLocation);
    }

    /**
     * Clears the user-related data from the session.
     * <p>
     * This method is typically called during logout to remove user-specific
     * information from the session.
     * </p>
     */
    public static void clearUserSession() {
        // Clear user data
        session.remove(USER_KEY);
        session.remove(RESTAURANT_KEY);
        session.remove(LOCATION_KEY);
    }

    //#region Filters

    public static void setFilters(FilterOptions filters) {
        session.put(FILTERS_KEY, filters);
    }

    public static FilterOptions getFilters() {
        return (FilterOptions) session.get(FILTERS_KEY);
    }

    //#endregion

}
