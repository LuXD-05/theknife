package uni.insubria.theknife.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import lombok.extern.slf4j.Slf4j;

import org.controlsfx.control.textfield.TextFields;

import uni.insubria.theknife.model.FilterOptions;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.Review;
import uni.insubria.theknife.model.Role;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.repository.RestaurantRepository;
import uni.insubria.theknife.repository.ReviewsRepository;
import uni.insubria.theknife.service.SessionService;
import uni.insubria.theknife.util.DistanceCalculator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Controller for the home screen of the TheKnife application.
 * <p>
 * This controller manages the main screen where users can:
 * </p>
 * <ul>
 *   <li>View a list of restaurants</li>
 *   <li>Filter restaurants by location</li>
 *   <li>Select a restaurant to view details</li>
 *   <li>Log out of the application</li>
 * </ul>
 * <p>
 * The controller handles different views based on user roles, showing only
 * relevant restaurants for restaurant owners.
 * </p>
 */
@Slf4j
public class HomeController {
    /**
     * Default constructor for the HomeController class.
     * <p>
     * This constructor is automatically called when the FXML loader creates an instance
     * of this controller. It doesn't perform any initialization; all initialization
     * is done in the initialize() method which is called after FXML loading.
     * </p>
     */
    public HomeController() {
        // Default constructor required by FXML loader
    }
    /**
     * Path to the restaurant detail view FXML file.
     */
    private static final String RESTAURANT_VIEW_PATH = "/view/restaurant.fxml";

    /**
     * Path to the login view FXML file.
     */
    private static final String LOGIN_VIEW_PATH = "/view/login.fxml";

    /**
     * Path to the filters view FXML file.
     */
    private static final String FILTERS_VIEW_PATH = "/view/filters.fxml";

    /**
     * List view displaying available restaurants.
     */
    @FXML
    private ListView<Restaurant> restaurantListView;

    /**
     * Map for save all restaurants
     */
    private Map<String, Restaurant> allRestaurants = new HashMap<>();

    private boolean toggled = false;

    /**
     * Label displaying welcome message with the current user's name.
     */
    @FXML
    private Label welcomeLabel;

    @FXML
    private Button openFiltersBtn;

    @FXML
    private Button clearFiltersBtn;

    @FXML
    private ToggleButton favoritesToggle;

    @FXML
    private ToggleButton reviewedToggle;

    @FXML
    private Label listPlaceholder;

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     * It sets up the user state, toggle buttons and displays the list of restaurants.
     */
    @FXML
    private void initialize() {
        
        initializeUserState();
        displayRestaurants();

    }

    /**
     * Initialize ListView
     */
    @FXML
    public void initializeListView() {
        // Load all restaurants only once, when the view is initialized
        allRestaurants = RestaurantRepository.loadRestaurants();

        restaurantListView.setItems(FXCollections.observableArrayList(allRestaurants.values()));
    }

    /**
     * Initializes the user state by setting the welcome message based on the current user.
     * If a user is logged in, displays their username; otherwise, shows "guest".
     */
    private void initializeUserState() {

        User user = SessionService.getUserFromSession();
        welcomeLabel.setText(String.format("Welcome %s!", user != null ? user.getUsername() : "guest"));

        Role role = user != null ? user.getRole() : null;

        // Client --> favorites + reviewed
        // Ristoratore --> no reviewed + toggle inserted
        // guest --> none
        if (role == Role.CLIENTE) {
            favoritesToggle.setVisible(true);
            favoritesToggle.setManaged(true);
            favoritesToggle.setText("Mostra preferiti");
            reviewedToggle.setVisible(true);
            reviewedToggle.setManaged(true);
        } else if (role == Role.RISTORATORE) {
            favoritesToggle.setVisible(false);
            favoritesToggle.setManaged(false);
            reviewedToggle.setVisible(false);
            reviewedToggle.setManaged(false);
        } else {
            favoritesToggle.setVisible(false);
            favoritesToggle.setManaged(false);
            reviewedToggle.setVisible(false);
            reviewedToggle.setManaged(false);
        }
        
    }

    /**
     * Displays the list of restaurants based on the current location and user role.
     * This method:
     * <ol>
     *   <li>Finds the reference coordinates for distance calculation</li>
     *   <li>Filters and sorts restaurants based on distance and user role</li>
     *   <li>Sets up the restaurant list view with the filtered restaurants</li>
     *   <li>Sets up the selection handler for restaurant clicks</li>
     * </ol>
     */
    public void displayRestaurants() {
        //Restaurant.Coordinate referenceCoordinates = findReferenceCoordinates();
        List<Restaurant> filteredRestaurants = getFilteredRestaurants(/*referenceCoordinates*/);
        setupRestaurantListView(filteredRestaurants);
        setupSelectionHandler();
    }

    /**
     * Finds the reference coordinates for distance calculations based on the current location.
     * <p>
     * This method searches for a restaurant in the current location and uses its
     * coordinates as a reference point for calculating distances to other restaurants.
     * </p>
     *
     * @return A Coordinate object representing the reference point, or null if no restaurant
     *         is found in the current location
     */
    // private Restaurant.Coordinate findReferenceCoordinates() {
    //     return SessionService.getRestaurants().stream()
    //         .filter(restaurant -> restaurant.getLocation().equals(SessionService.getFilters().getLocation()))
    //         .map(restaurant -> new Restaurant.Coordinate(restaurant.getLongitude(), restaurant.getLatitude()))
    //         .findFirst()
    //         .orElse(null);
    // }

    /**
     * Filters and sorts the list of restaurants based on user role and distance.
     * <p>
     * This method:
     * </p>
     * <ul>
     *   <li>For regular users and guests, shows all restaurants</li>
     *   <li>For restaurant owners, shows only their own restaurants</li>
     *   <li>Updates the distance for each restaurant from the reference coordinates</li>
     *   <li>Sorts restaurants by distance (closest first)</li>
     * </ul>
     *
     * @param referenceCoordinates The coordinates to calculate distances from
     * @return A filtered and sorted list of Restaurant objects
     */
    private List<Restaurant> getFilteredRestaurants(/*Restaurant.Coordinate referenceCoordinates*/) {
        //! NEW CODE --> always filter
        FilterOptions filters = SessionService.getFilters();
        User user = SessionService.getUserFromSession();
        return SessionService.getRestaurants().stream()
            .filter(restaurant -> user == null || !Role.RISTORATORE.equals(user.getRole()) || user.getRestaurants().contains(restaurant))
            .filter(restaurant -> filters == null || filters.matches(restaurant))
            //.peek(restaurant -> updateRestaurantDistance(restaurant, referenceCoordinates))
            .sorted(Comparator.comparing(Restaurant::getName, String.CASE_INSENSITIVE_ORDER)) // Comparator.comparingDouble(Restaurant::getDistance)
            .collect(Collectors.toList());
            
        //! OLD CODE --> doesn't always filter
        // return SessionService.getRestaurants().stream()
        //         .filter(restaurant -> SessionService.getUserFromSession() == null || !Role.RISTORATORE.equals(SessionService.getUserFromSession().getRole()) || SessionService.getUserFromSession().getRestaurants().contains(restaurant))
        //         .peek(restaurant -> updateRestaurantDistance(restaurant, referenceCoordinates))
        //         .sorted(Comparator.comparingDouble(Restaurant::getDistance))
        //         .collect(Collectors.toList());
    }

    /**
     * Updates the distance property of a restaurant based on reference coordinates.
     * <p>
     * This method calculates the distance between the restaurant and the reference coordinates
     * using the Haversine formula, and sets the distance property on the restaurant object.
     * </p>
     *
     * @param restaurant The restaurant to update with distance information
     * @param referenceCoordinates The reference coordinates to calculate distance from,
     *                            or null if no reference coordinates are available
     */
    // private void updateRestaurantDistance(Restaurant restaurant, Restaurant.Coordinate referenceCoordinates) {
    //     if (referenceCoordinates != null) {
    //         double distance = DistanceCalculator.calculateDistanceInKm(restaurant, referenceCoordinates);
    //         restaurant.setDistance(distance);
    //     }
    // }

    /**
     * Sets up the restaurant list view with the provided list of restaurants.
     * <p>
     * This method:
     * </p>
     * <ul>
     *   <li>Creates an observable list from the provided restaurant list</li>
     *   <li>Sets the items in the restaurant list view</li>
     *   <li>Configures the cell factory to customize how restaurants are displayed</li>
     * </ul>
     *
     * @param restaurants The list of Restaurant objects to display in the list view
     */
    private void setupRestaurantListView(List<Restaurant> restaurants) {
        ObservableList<Restaurant> restaurantList = FXCollections.observableArrayList(restaurants);
        restaurantListView.setItems(restaurantList);
        restaurantListView.setCellFactory(this::createRestaurantCell);
    }

    
    // TextField used to capture the user's input for restaurant name search
    @FXML
    private TextField searchField;

    /**
     * Handles the real-time search of restaurants based on user input in the search field.
     * 
     * This method is triggered every time a key is released inside the search TextField.
     * It filters the currently visible list of restaurants by matching the input text
     * with the restaurant names (case-insensitive). If the input is empty, it resets
     * the view by displaying the original filtered list (by city and user role).
     *
     * @param event The KeyEvent triggered by typing in the search TextField
     */
    @FXML
    private void handleSearch(KeyEvent event) {
        // Get the current input from the search field
        String query = searchField.getText();

        // If the query is blank or null, restore the original list (filtered by city and user role)
        if (query == null || query.isBlank()) {
            displayRestaurants();
            return;
        }

        // Get the currently displayed list of restaurants
        List<Restaurant> currentRestaurants = restaurantListView.getItems();

        // Filter the current list based on the search query
        List<Restaurant> results = RestaurantRepository.searchRestaurants(currentRestaurants, query);

        // Update the ListView with the filtered results
        restaurantListView.setItems(FXCollections.observableArrayList(results));
    }

    /**
     * Creates a custom cell factory for the restaurant list view.
     * <p>
     * This method returns a ListCell implementation that displays restaurant information
     * in a formatted way using the formatRestaurantText method.
     * </p>
     *
     * @param lv The ListView that will use this cell factory
     * @return A ListCell implementation for displaying restaurant information
     */
    private ListCell<Restaurant> createRestaurantCell(ListView<Restaurant> lv) {
        return new ListCell<>() {
            @Override
            protected void updateItem(Restaurant restaurant, boolean empty) {
                super.updateItem(restaurant, empty);
                setText(empty ? null : formatRestaurantText(restaurant));
            }
        };
    }

    /**
     * Formats restaurant information into a display string.
     * <p>
     * This method creates a formatted string containing the restaurant's name,
     * location, and distance from the reference point.
     * </p>
     *
     * @param restaurant The Restaurant object to format
     * @return A formatted string representation of the restaurant
     */
    private String formatRestaurantText(Restaurant restaurant) {
        return String.format("%s - %s"/* - (%.0fkm)*/, restaurant.getName(), restaurant.getLocation()/*, restaurant.getDistance()*/);
    }

    /**
     * Sets up the event handler for restaurant selection in the list view.
     * <p>
     * This method adds a mouse click event filter to the restaurant list view
     * that triggers the handleRestaurantSelection method when a restaurant is clicked.
     * </p>
     */
    private void setupSelectionHandler() {
        restaurantListView.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> handleRestaurantSelection());
    }

    /**
     * Handles the selection of a restaurant from the list view.
     * <p>
     * This method is called when a user clicks on a restaurant in the list view.
     * It retrieves the selected restaurant, stores it in the session, and
     * navigates to the restaurant detail view.
     * </p>
     */
    @FXML
    private void handleRestaurantSelection() {
        try {
            Restaurant selectedRestaurant = restaurantListView.getSelectionModel().getSelectedItem();
            if (selectedRestaurant == null)
                return;
            SessionService.setRestaurantInSession(selectedRestaurant);
            navigateToRestaurantView();
        } catch (IOException e) {
            log.error("Error handling restaurant selection", e);
        }
    }

    /**
     * Navigates to the restaurant detail view.
     * <p>
     * This method loads the restaurant detail view FXML file and sets it as the
     * current scene in the application using the SessionService.
     * </p>
     *
     * @throws IOException If an error occurs during loading the FXML file or setting the scene
     */
    private void navigateToRestaurantView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(RESTAURANT_VIEW_PATH));
        SessionService.setSceneInSession(fxmlLoader);
    }

    @FXML
    public void handleOpenFilters() throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FILTERS_VIEW_PATH));
        SessionService.setSceneInSession(fxmlLoader);
    }

    @FXML
    private void handleClearFilters() {
        // Resets filters & re-display restaurants
        SessionService.setFilters(new FilterOptions());
        displayRestaurants();
    }

    @FXML
    private void handleFavoritesToggle() {

        reviewedToggle.setSelected(false);
        toggled = favoritesToggle.isSelected();

        searchField.setDisable(toggled);
        openFiltersBtn.setDisable(toggled);
        clearFiltersBtn.setDisable(toggled);
        
        // Shows favorites if toggled, otherwise restaurants normally
        if (toggled) {
            User user = SessionService.getUserFromSession();
            listPlaceholder.setText("Nessun ristorante preferito.");
            setupRestaurantListView(new ArrayList<>(user.getRestaurants()));
        } else {
            listPlaceholder.setText("Nessun ristorante trovato per la location selezionata.");
            displayRestaurants();
        }

    }

    @FXML
    private void handleReviewedToggle() {

        favoritesToggle.setSelected(false);
        toggled = reviewedToggle.isSelected();

        searchField.setDisable(toggled);
        openFiltersBtn.setDisable(toggled);
        clearFiltersBtn.setDisable(toggled);
        
        // Shows reviewed if toggled, otherwise restaurants normally
        if (toggled) {
            listPlaceholder.setText("Nessun ristorante recensito.");

            // Get user in session & all restaurants
            User user = SessionService.getUserFromSession();
            List<Restaurant> restaurants = SessionService.getRestaurants();

            // Get only restaurants reviewed by user in session + display them
            List<Restaurant> reviewedRestaurants = ReviewsRepository.loadReviews().values().stream()
                .filter(review -> review.getUser() != null && review.getUser().getUsername().equals(user.getUsername()))
                .map(review -> {
                    String restaurantId = review.getRestaurant().getId();
                    return restaurants.stream()
                        .filter(r -> r.getId().equals(restaurantId))
                        .findFirst()
                        .orElse(null);
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
            setupRestaurantListView(reviewedRestaurants);
        } else {
            listPlaceholder.setText("Nessun ristorante trovato per la location selezionata.");
            displayRestaurants();
        }

    }

    /**
     * Handles the logout action.
     * Navigates to the login view and clears the user session.
     *
     * @param actionEvent The event that triggered the logout action
     * @throws RuntimeException If there is an error during navigation to the login view
     */
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
