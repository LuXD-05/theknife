/* 
Mordente Marcello 761730 VA
Luciano Alessio 759956 VA
Nardo Luca 761132 VA
Morosini Luca 760029 VA
*/
package uni.insubria.theknife.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import lombok.extern.slf4j.Slf4j;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import uni.insubria.theknife.model.FilterOptions;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.Review;
import uni.insubria.theknife.model.Role;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.repository.RestaurantRepository;
import uni.insubria.theknife.repository.ReviewsRepository;
import uni.insubria.theknife.service.AlertService;
import uni.insubria.theknife.service.SessionService;
import uni.insubria.theknife.util.DistanceCalculator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
    private Button addRestaurantBtn;

    @FXML
    private Label listPlaceholder;

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     * It sets up the user state, toggle buttons and displays the list of restaurants.
     */
    /** City selection field (mandatory): the restaurant search is scoped to one city. */
    @FXML
    private TextField cityField;

    @FXML
    private void initialize() {

        initializeUserState();
        setupCityField();
        displayRestaurants();

        // Refresh the list in real time when the backend broadcasts catalog changes
        // (unless a favorites/reviewed toggle is currently active).
        SessionService.setOnDataChanged(() -> {
            if (!toggled) {
                displayRestaurants();
            }
        });
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
            addRestaurantBtn.setVisible(false);
            addRestaurantBtn.setManaged(false);

        } else if (role == Role.RISTORATORE) {
            favoritesToggle.setVisible(false);
            favoritesToggle.setManaged(false);
            reviewedToggle.setVisible(false);
            reviewedToggle.setManaged(false);
            addRestaurantBtn.setVisible(true);
            addRestaurantBtn.setManaged(true);

        } else {
            favoritesToggle.setVisible(false);
            favoritesToggle.setManaged(false);
            reviewedToggle.setVisible(false);
            reviewedToggle.setManaged(false);
            addRestaurantBtn.setVisible(false);
            addRestaurantBtn.setManaged(false);
        }

        // City search applies to CLIENTE/guest only; the RISTORATORE sees owned restaurants.
        boolean ownerRole = role == Role.RISTORATORE;
        if (cityField != null) {
            cityField.setVisible(!ownerRole);
            cityField.setManaged(!ownerRole);
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


    private void addNewRestaurantToCurrentUser(Restaurant newRestaurant) {
        User currentUser = SessionService.getUserFromSession();
        if (currentUser != null) {
            // Associa il ristorante all'utente
            newRestaurant.setUser(currentUser);

            // Aggiungi il ristorante alla collezione dell'utente
            currentUser.getRestaurants().add(newRestaurant);

            // Aggiorna la sessione con l'utente modificato
            SessionService.setUserInSession(currentUser);

            // Se vuoi aggiornare la ListView usando displayRestaurants, chiamalo qui
            displayRestaurants();
        } else {
            System.err.println("Nessun utente loggato, impossibile associare il ristorante.");
        }
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
    private List<Restaurant> getFilteredRestaurants() {
        User user = SessionService.getUserFromSession();
        FilterOptions filters = SessionService.getFilters();

        // The RISTORATORE sees the restaurants they OWN, fetched by owner from the backend,
        // regardless of the currently selected city (their restaurants may be in any city).
        if (user != null && Role.RISTORATORE.equals(user.getRole())) {
            return RestaurantRepository.loadMyRestaurants().values().stream()
                    .sorted(Comparator.comparing(Restaurant::getName, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        }

        // CLIENTE / guest: the restaurants of the selected city, with client-side filters.
        return SessionService.getRestaurants().stream()
                .filter(r -> filters == null || filters.matches(r))
                .sorted(Comparator.comparing(Restaurant::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
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
     * Configures the city field: autocomplete over the known locations, prefill with the
     * current city (the logged user's city or the location already chosen), and trigger the
     * search on Enter. The restaurant search is always scoped to a city.
     */
    private void setupCityField() {
        if (cityField == null) {
            return;
        }
        AutoCompletionBinding<String> binding = TextFields.bindAutoCompletion(cityField, param -> {
            String userText = param.getUserText().toLowerCase();
            return SessionService.getLocations().stream()
                    .filter(l -> l.toLowerCase().contains(userText))
                    .collect(Collectors.toList());
        });
        // Run the search automatically as soon as a city is picked from the dropdown.
        binding.setOnAutoCompleted(e -> handleCitySearch());

        FilterOptions filters = SessionService.getFilters();
        String current = (filters != null && filters.getLocation() != null && !filters.getLocation().isBlank())
                ? filters.getLocation()
                : SessionService.getLocation();
        cityField.setText(current == null ? "" : current);
        // Enter also triggers the search (e.g. when the city is typed in full).
        cityField.setOnAction(e -> handleCitySearch());
    }

    /**
     * Handles the city-scoped search: the city is mandatory and must be a known location.
     * On success it sets the active location and reloads the restaurants for that city.
     */
    @FXML
    private void handleCitySearch() {
        String selected = cityField.getText() == null ? "" : cityField.getText().trim();
        if (selected.isBlank()) {
            AlertService.alert(Alert.AlertType.WARNING, "ATTENZIONE", "Città obbligatoria",
                    "Seleziona una città prima di effettuare la ricerca.");
            return;
        }
        String canonical = SessionService.getLocations().stream()
                .filter(l -> l.equalsIgnoreCase(selected))
                .findFirst()
                .orElse(null);
        if (canonical == null) {
            AlertService.alert(Alert.AlertType.WARNING, "ATTENZIONE", "Città non valida",
                    "Seleziona una città dall'elenco dei suggerimenti.");
            return;
        }
        cityField.setText(canonical);

        FilterOptions filters = SessionService.getFilters();
        if (filters == null) {
            filters = new FilterOptions();
            SessionService.setFilters(filters);
        }
        filters.setLocation(canonical);
        SessionService.setLocation(canonical);

        if (searchField != null) {
            searchField.clear();
        }
        displayRestaurants();
    }

    /**
     * Handles the real-time search of restaurants based on user input in the search field.
     * <p>
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
    public void handleOpenFilters() throws IOException {
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


    //#region HANDLE ADD RESTAURANTS
    @FXML
    private void handleAddRestaurant() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Restaurant");
        dialog.setHeaderText("Enter new restaurant details");

        TextField nameField = new TextField();
        TextField addressField = new TextField();
        TextField locationField = new TextField();
        TextField phoneField = new TextField();
        TextField websiteField = new TextField();

        List<String> availableCuisines = SessionService.getCuisines();

        ComboBox<String> cuisineCombo = new ComboBox<>();
        cuisineCombo.getItems().addAll(availableCuisines);
        cuisineCombo.setEditable(true);
        cuisineCombo.setPromptText("Type or select cuisine");

        AutoCompletionBinding<String> cuisineBinding = TextFields.bindAutoCompletion(cuisineCombo.getEditor(), param -> {
            String userText = param.getUserText() == null ? "" : param.getUserText().toLowerCase();

            return availableCuisines.stream()
                    .filter(cuisine -> cuisine.toLowerCase().contains(userText))
                    .collect(Collectors.toList());
        });

        cuisineBinding.setOnAutoCompleted(event -> cuisineCombo.setValue(event.getCompletion()));

        ComboBox<String> priceCombo = new ComboBox<>();
        priceCombo.getItems().addAll("$", "$$", "$$$", "$$$$");
        priceCombo.setPromptText("Select price range");

        TextField longitudeField = new TextField();
        longitudeField.setPromptText("Optional");

        TextField latitudeField = new TextField();
        latitudeField.setPromptText("Optional");

        ComboBox<String> awardCombo = new ComboBox<>();
        awardCombo.getItems().addAll(
                "Selected Restaurants",
                "Bib Gourmand",
                "1 Star",
                "2 Stars",
                "3 Stars"
        );
        awardCombo.setPromptText("Select award");

        TextField greenStarField = new TextField();
        greenStarField.setPromptText("Optional: 0 or 1");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(4);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(locationField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Cuisine:"), 0, 4);
        grid.add(cuisineCombo, 1, 4);
        grid.add(new Label("Website URL:"), 0, 5);
        grid.add(websiteField, 1, 5);
        grid.add(new Label("Price:"), 0, 6);
        grid.add(priceCombo, 1, 6);
        grid.add(new Label("Longitude (optional):"), 0, 7);
        grid.add(longitudeField, 1, 7);
        grid.add(new Label("Latitude (optional):"), 0, 8);
        grid.add(latitudeField, 1, 8);
        grid.add(new Label("Award:"), 0, 9);
        grid.add(awardCombo, 1, 9);
        grid.add(new Label("Green Star:"), 0, 10);
        grid.add(greenStarField, 1, 10);
        grid.add(new Label("Description:"), 0, 11);
        grid.add(descriptionArea, 1, 11);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String error = validateNewRestaurantFields(
                    nameField,
                    addressField,
                    locationField,
                    phoneField,
                    websiteField,
                    longitudeField,
                    latitudeField,
                    greenStarField,
                    priceCombo,
                    cuisineCombo,
                    awardCombo
            );

            if (error != null) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, error);
                errorAlert.showAndWait();
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Restaurant newRestaurant = new Restaurant();

                newRestaurant.setName(nameField.getText().trim());
                newRestaurant.setAddress(addressField.getText().trim());
                newRestaurant.setLocation(locationField.getText().trim());
                newRestaurant.setPhone(phoneField.getText().trim());
                newRestaurant.setCuisine(getComboText(cuisineCombo));
                newRestaurant.setWebsiteUrl(websiteField.getText().trim());
                newRestaurant.setPrice(priceCombo.getValue());

                if (longitudeField.getText() == null || longitudeField.getText().isBlank()) {
                    newRestaurant.setLongitude(null);
                } else {
                    newRestaurant.setLongitude(Float.parseFloat(longitudeField.getText().trim()));
                }

                if (latitudeField.getText() == null || latitudeField.getText().isBlank()) {
                    newRestaurant.setLatitude(null);
                } else {
                    newRestaurant.setLatitude(Float.parseFloat(latitudeField.getText().trim()));
                }

                newRestaurant.setAward(awardCombo.getValue());

                if (greenStarField.getText() == null || greenStarField.getText().isBlank()) {
                    newRestaurant.setGreenStar(null);
                } else {
                    newRestaurant.setGreenStar(Integer.parseInt(greenStarField.getText().trim()));
                }

                newRestaurant.setDescription(descriptionArea.getText());
                newRestaurant.setId(RestaurantRepository.generateUniqueId(newRestaurant));

                RestaurantRepository.ERROR_CODE result = RestaurantRepository.addRestaurant(newRestaurant);

                if (result == RestaurantRepository.ERROR_CODE.NONE) {
                    addNewRestaurantToCurrentUser(newRestaurant);
                    displayRestaurants();

                    Alert info = new Alert(Alert.AlertType.INFORMATION, "New restaurant added successfully!");
                    info.showAndWait();
                } else if (result == RestaurantRepository.ERROR_CODE.DUPLICATED) {
                    Alert warn = new Alert(Alert.AlertType.WARNING, "A restaurant with these details already exists.");
                    warn.showAndWait();
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR, "Failed to add new restaurant due to a service error.");
                    error.showAndWait();
                }
            }
        });
    }

    private String validateNewRestaurantFields(
            TextField nameField,
            TextField addressField,
            TextField locationField,
            TextField phoneField,
            TextField websiteField,
            TextField longitudeField,
            TextField latitudeField,
            TextField greenStarField,
            ComboBox<String> priceCombo,
            ComboBox<String> cuisineCombo,
            ComboBox<String> awardCombo
    ) {
        if (nameField.getText() == null || nameField.getText().isBlank()) {
            return "Name is mandatory.";
        }

        if (addressField.getText() == null || addressField.getText().isBlank()) {
            return "Address is mandatory.";
        }

        if (locationField.getText() == null || locationField.getText().isBlank()) {
            return "Location is mandatory.";
        }

        if (locationField.getText().matches(".*\\d.*")) {
            return "Location cannot contain numbers.";
        }

        if (priceCombo.getValue() == null) {
            return "Price range is mandatory.";
        }

        String selectedCuisine = getComboText(cuisineCombo);
        if (selectedCuisine.isBlank()) {
            return "Cuisine is mandatory.";
        }

        boolean validCuisine = SessionService.getCuisines().stream()
                .anyMatch(cuisine -> cuisine.equalsIgnoreCase(selectedCuisine));

        if (!validCuisine) {
            return "Select a valid cuisine from the suggestions.";
        }

        if (awardCombo.getValue() == null || awardCombo.getValue().isBlank()) {
            return "Award is mandatory.";
        }

        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        String phoneRegex = "^\\+?[0-9. ()-]{7,25}$";
        if (!phone.isBlank() && !phone.matches(phoneRegex)) {
            return "Invalid international phone number format.";
        }

        String website = websiteField.getText() == null ? "" : websiteField.getText().trim();
        String urlRegex = "^(https?://)?([\\w.-]+)\\.([a-z]{2,6})([/\\w .-]*)*/?$";
        if (!website.isBlank() && !website.matches(urlRegex)) {
            return "Invalid website URL format.";
        }

        String longitude = longitudeField.getText() == null ? "" : longitudeField.getText().trim();
        if (!longitude.isBlank()) {
            try {
                float lon = Float.parseFloat(longitude);
                if (lon < -180f || lon > 180f) {
                    return "Longitude must be between -180 and 180.";
                }
            } catch (NumberFormatException e) {
                return "Longitude must be a valid number.";
            }
        }

        String latitude = latitudeField.getText() == null ? "" : latitudeField.getText().trim();
        if (!latitude.isBlank()) {
            try {
                float lat = Float.parseFloat(latitude);
                if (lat < -90f || lat > 90f) {
                    return "Latitude must be between -90 and 90.";
                }
            } catch (NumberFormatException e) {
                return "Latitude must be a valid number.";
            }
        }

        String greenStar = greenStarField.getText() == null ? "" : greenStarField.getText().trim();
        if (!greenStar.isBlank()) {
            try {
                int value = Integer.parseInt(greenStar);
                if (value != 0 && value != 1) {
                    return "Green Star must be 0 or 1.";
                }
            } catch (NumberFormatException e) {
                return "Green Star must be a valid number.";
            }
        }

        return null;
    }

    
    private String getComboText(ComboBox<String> comboBox) {
    if (comboBox == null) {
        return "";
    }

    String editorText = comboBox.isEditable() && comboBox.getEditor() != null
            ? comboBox.getEditor().getText()
            : null;

    if (editorText != null && !editorText.isBlank()) {
        return editorText.trim();
    }

    String value = comboBox.getValue();
    return value == null ? "" : value.trim();
}
    //#endregion
}
