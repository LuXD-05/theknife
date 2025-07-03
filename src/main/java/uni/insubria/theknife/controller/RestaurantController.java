package uni.insubria.theknife.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.Review;
import uni.insubria.theknife.model.Role;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.repository.RestaurantRepository;
import uni.insubria.theknife.repository.ReviewsRepository;
import uni.insubria.theknife.service.AlertService;
import uni.insubria.theknife.service.SessionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javafx.geometry.Insets;
import uni.insubria.theknife.repository.UserRepository;

import static uni.insubria.theknife.util.Messages.*;

/**
 * Controller for the restaurant detail screen of the TheKnife application.
 * <p>
 * This controller manages the restaurant detail view where users can:
 * </p>
 * <ul>
 *   <li>View detailed information about a selected restaurant</li>
 *   <li>Read reviews from other users</li>
 *   <li>Add their own reviews (for customers)</li>
 *   <li>Edit or delete their own reviews</li>
 *   <li>Respond to reviews (for restaurant owners)</li>
 * </ul>
 * <p>
 * The controller handles different views and actions based on user roles:
 * </p>
 * <ul>
 *   <li>Customers can add, edit, and delete their own reviews</li>
 *   <li>Restaurant owners can respond to reviews but cannot add reviews</li>
 *   <li>Guests can view restaurant details and reviews but cannot add reviews</li>
 * </ul>
 */
public class RestaurantController {
    /**
     * Default constructor for the RestaurantController class.
     * <p>
     * This constructor is automatically called when the FXML loader creates an instance
     * of this controller. It doesn't perform any initialization; all initialization
     * is done in the initialize() method which is called after FXML loading.
     * </p>
     */
    public RestaurantController() {
        // Default constructor required by FXML loader
    }
    /**
     * Container for the review editing interface.
     */
    @FXML
    private VBox editReviewBox;

    /**
     * Text field for editing review content.
     */
    @FXML
    private TextField editReviewContent;

    /**
     * Dropdown selector for editing review rating.
     */
    @FXML
    private ComboBox<Integer> editRatingSelector;

    /**
     * The review currently being edited.
     */
    private Review currentlyEditingReview;

    /**
     * Labels for displaying basic restaurant information.
     */
    @FXML
    private Label nameLabel, descriptionLabel, addressLabel, priceLabel;

    /**
     * Labels for displaying restaurant location and contact information.
     */
    @FXML
    private Label cuisineLabel, longitudeLabel, latitudeLabel, phoneLabel;

    /**
     * Labels for displaying restaurant web links and awards.
     */
    @FXML
    private Label michelinUrlLabel, websiteUrlLabel, awardLabel;

    /**
     * Labels for displaying additional restaurant features and messages.
     */
    @FXML
    private Label greenStarLabel, facilitiesLabel, registerMessage;
    /**
     * Button for adding a new review.
     */
    @FXML
    private Button addReview;

    /**
     * Container for the review input interface.
     */
    @FXML
    private VBox addReviewBox;

    /**
     * Dropdown selector for selecting a rating when adding a new review.
     */
    @FXML
    private ComboBox<String> ratingSelector;

    /**
     * Text field for entering review content when adding a new review.
     */
    @FXML
    private TextField reviewContent;

    /**
     * Label displaying welcome message with the current user's name.
     */
    @FXML
    private Label welcomeLabel;

    /**
     * List view displaying all reviews for the current restaurant.
     */
    @FXML
    private ListView<Review> reviewsListView;

    /**
     * Label displaying the total number of reviews for the restaurant.
     */
    @FXML
    private Label totalReviewsLabel;

    /**
     * Label displaying the average rating for the restaurant.
     */
    @FXML
    private Label averageRatingLabel;

    @FXML
    private Button toggleFavorite;

    @FXML
    private Button editRestaurant;

    @FXML
    private Button deleteRestaurant;
    
    /**
     * Observable list of reviews for binding to the reviews list view.
     */
    private final ObservableList<Review> reviewObservableList = FXCollections.observableArrayList();

    /**
     * The currently selected review in the list view.
     */
    private Review selectedReview;

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
        // Initialize edit review components
        editRatingSelector.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        editReviewBox.setVisible(false);
        initializeUserState();
        initializeRatingSelector();
        populateRestaurantDetails();

    }

    /**
     * Initializes the list of reviews for a restaurant.
     * <p>
     * This method:
     * </p>
     * <ul>
     *   <li>Ensures the restaurant has a reviews list (creates an empty one if needed)</li>
     *   <li>Sets up the review list view with the restaurant's reviews</li>
     *   <li>Configures the selection listener for reviews</li>
     * </ul>
     * <p>
     * The selection listener is configured to track the selected review
     * for restaurant owners who can respond to reviews.
     * </p>
     *
     * @param restaurant The Restaurant object whose reviews will be displayed
     */
    private void initializeReviewsList(Restaurant restaurant) {
        if (restaurant.getReviews() == null) {
            restaurant.setReviews(new ArrayList<>());
        }
        setupReviewListView(restaurant.getReviews());
        reviewsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && SessionService.getUserFromSession() != null && SessionService.getUserFromSession().getRole() == Role.RISTORATORE) {
                selectedReview = newVal;
            }
        });
    }

    /**
     * Initializes the UI based on the current user's state.
     * <p>
     * This method:
     * </p>
     * <ul>
     *   <li>Sets the welcome message with the current user's name</li>
     *   <li>Configures the review interface based on the user's role</li>
     *   <li>Disables the add review button for guests</li>
     *   <li>Shows appropriate messages for guests</li>
     * </ul>
     * <p>
     * The UI is configured differently for:
     * </p>
     * <ul>
     *   <li>Restaurant owners (can respond to reviews but not add them)</li>
     *   <li>Customers (can add, edit, and delete their own reviews)</li>
     *   <li>Guests (can view reviews but not add them)</li>
     * </ul>
     */
    private void initializeUserState() {

        User user = SessionService.getUserFromSession();
        welcomeLabel.setText(String.format("Welcome %s!", user != null ? user.getUsername() : "guest"));

        if (user != null) {
            switch (user.getRole()) {
                case CLIENTE:
                    toggleFavorite.setVisible(true);
                    editRestaurant.setVisible(false);
                    deleteRestaurant.setVisible(false);
                    Restaurant r = SessionService.getRestaurantFromSession().orElse(null);
                    if (user.getRestaurants().contains(r))
                        toggleFavorite.setText("★");
                    else
                        toggleFavorite.setText("☆");
                    break;
                case RISTORATORE:
                    toggleFavorite.setVisible(false);
                    editRestaurant.setVisible(true);
                    deleteRestaurant.setVisible(true);
                    addReviewBox.setVisible(false);
                    break;
                default: 
                    toggleFavorite.setVisible(false);
                    editRestaurant.setVisible(false);
                    deleteRestaurant.setVisible(false);
                    break;
            }
        } else {
            toggleFavorite.setVisible(false);
            editRestaurant.setVisible(false);
            deleteRestaurant.setVisible(false);
        }

        if (addReview != null) {
            if (user == null) {
                addReview.setDisable(true);
                if (registerMessage != null) {
                    registerMessage.setVisible(true);
                    registerMessage.setText(LOGIN_PROMPT);
                }
            } else {
                addReview.setDisable(false);
                if (registerMessage != null) {
                    registerMessage.setVisible(false);
                }
            }
        }
    }

    /**
     * Initializes the rating selector dropdown.
     * <p>
     * This method populates the rating selector dropdown with values from 1 to 5,
     * representing the star ratings that users can select when adding a review.
     * </p>
     */
    private void initializeRatingSelector() {
        if (ratingSelector != null) {
            ratingSelector.setItems(FXCollections.observableArrayList("1 star", "2 stars", "3 stars", "4 stars", "5 stars"));
        }
    }

    /**
     * Populates the restaurant details in the UI.
     * <p>
     * This method retrieves the current restaurant from the session and
     * passes it to the displayRestaurantInfo method to populate the UI
     * with the restaurant's details.
     * </p>
     */
    private void populateRestaurantDetails() {
        Optional<Restaurant> optionalRestaurant = SessionService.getRestaurantFromSession();
        optionalRestaurant.ifPresent(this::displayRestaurantInfo);
    }

    /**
     * Handles the add review button click event.
     * <p>
     * This method validates the review input, creates a new review with the
     * provided content and rating, and adds it to the repository. If successful,
     * the review is added to the restaurant's review list and displayed in the UI.
     * </p>
     * <p>
     * The method performs several validation checks:
     * </p>
     * <ul>
     *   <li>Verifies that both review content and rating are provided</li>
     *   <li>Verifies that a user is logged in</li>
     *   <li>Verifies that a restaurant is selected</li>
     * </ul>
     */
    @FXML
    private void handleAddReview() {
        if (!validateReviewInput()) {
            showAlert(MISSING_INFO_TITLE, REVIEW_VALIDATION_MESSAGE);
            return;
        }

        User user = validateUser();
        Restaurant restaurant = validateRestaurant();
        if (user == null || restaurant == null) {
            return;
        }

        // Check if user has already reviewed this restaurant
        boolean hasExistingReview = restaurant.getReviews().stream().anyMatch(review -> review.getUser().getUsername().equals(user.getUsername()));

        if (hasExistingReview) {
            showAlert(MISSING_INFO_TITLE, "You have already reviewed this restaurant.");
            return;
        }

        Review newReview = createReview(user, restaurant);

        // Persist the review
        ReviewsRepository.ERROR_CODE result = ReviewsRepository.addReview(newReview);
        if (result != ReviewsRepository.ERROR_CODE.NONE) {
            AlertService.alert(Alert.AlertType.ERROR, "ATTENZIONE", null, "Non è possibile inserire piu' recensioni per questo ristorante.");
            return;
        }

        // Update the ListView
        setupReviewListView(restaurant.getReviews());


        clearReviewInputs();
    }

    /**
     * Displays the detailed information of a restaurant in the UI.
     * <p>
     * This method populates all the UI components with the restaurant's information,
     * including name, description, address, price, cuisine, coordinates, contact information,
     * and Michelin-specific data. It also initializes the review list for the restaurant.
     * </p>
     *
     * @param restaurant The Restaurant object whose information will be displayed
     */
    private void displayRestaurantInfo(Restaurant restaurant) {
        if (restaurant == null) {
            return;
        }

        try {
            setLabelText(nameLabel, restaurant.getName());
            setLabelText(descriptionLabel, restaurant.getDescription());
            setLabelText(addressLabel, restaurant.getAddress());
            setLabelText(priceLabel, restaurant.getPrice());
            setLabelText(cuisineLabel, restaurant.getCuisine());
            setLabelText(longitudeLabel, restaurant.getLongitude() != 0 ? String.valueOf(restaurant.getLongitude()) : "");
            setLabelText(latitudeLabel, restaurant.getLatitude() != 0 ? String.valueOf(restaurant.getLatitude()) : "");
            setLabelText(phoneLabel, restaurant.getPhone());
            setLabelText(michelinUrlLabel, restaurant.getMichelinUrl());
            setLabelText(websiteUrlLabel, restaurant.getWebsiteUrl());
            setLabelText(awardLabel, restaurant.getAward());
            setLabelText(greenStarLabel, restaurant.getGreenStar() != 0 ? String.valueOf(restaurant.getGreenStar()) : "");
            setLabelText(facilitiesLabel, restaurant.getFacilities());

            initializeReviewsList(restaurant);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load restaurant information.");
        }
    }

    /**
     * Sets the text of a label, handling null values gracefully.
     * <p>
     * This method sets the text of the specified label to the provided text,
     * or to an empty string if the text is null. It also checks that the label
     * itself is not null before attempting to set its text.
     * </p>
     *
     * @param label The Label object to set text on
     * @param text The text to set on the label
     */
    private void setLabelText(Label label, String text) {
        if (label != null) {
            label.setText(text != null ? text : "");
        }
    }


    /**
     * Validates the review input.
     * <p>
     * This method checks that both the review content and rating are provided.
     * </p>
     *
     * @return true if both review content and rating are provided, false otherwise
     */
    private boolean validateReviewInput() {
        return !reviewContent.getText().isEmpty() && ratingSelector.getValue() != null;
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
     * Creates a new review object with the provided user, restaurant, content, and rating.
     * <p>
     * This method:
     * </p>
     * <ul>
     *   <li>Extracts the numeric rating from the rating selector</li>
     *   <li>Creates a unique ID for the review</li>
     *   <li>Sets the user, restaurant, content, and star rating</li>
     * </ul>
     *
     * @param user The User object creating the review
     * @param restaurant The Restaurant object being reviewed
     * @return A fully populated Review object
     */
    private Review createReview(User user, Restaurant restaurant) {
        String ratingText = ratingSelector.getValue();
        int stars = Character.getNumericValue(ratingText.charAt(0));
        return new Review().setId(String.valueOf(Objects.hash(user.getUsername(), restaurant.getId()))).setUser(user).setRestaurant(restaurant).setContent(reviewContent.getText()).setStars(stars);
    }

    /**
     * Clears the review input fields.
     * <p>
     * This method resets the review content text field and rating selector
     * to their default empty states after a review is submitted or when
     * the form needs to be cleared.
     * </p>
     */
    private void clearReviewInputs() {
        reviewContent.clear();
        ratingSelector.getSelectionModel().clearSelection();
    }

    /**
     * Displays an alert dialog with the specified title and content.
     * <p>
     * This method creates and shows a warning alert dialog with the provided
     * title and content. The header text is set to null for a cleaner appearance.
     * </p>
     *
     * @param title The title text for the alert dialog
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
            SessionService.setRestaurantInSession(null);
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

    /**
     * Updates the review summary information displayed in the UI.
     * <p>
     * This method calculates and displays:
     * </p>
     * <ul>
     *   <li>The total number of reviews for the restaurant</li>
     *   <li>The average rating across all reviews</li>
     * </ul>
     *
     * @param reviews The list of Review objects for the restaurant
     */
    private void updateReviewSummary(List<Review> reviews) {
        int totalReviews = reviews.size();
        double averageRating = reviews.stream().mapToInt(Review::getStars).average().orElse(0.0);

        totalReviewsLabel.setText(String.valueOf(totalReviews));
        averageRatingLabel.setText(String.format("%.1f ★", averageRating));
    }

    /**
     * Sets up the list view for displaying reviews.
     * <p>
     * This method:
     * </p>
     * <ul>
     *   <li>Updates the review summary information</li>
     *   <li>Clears and repopulates the observable list of reviews</li>
     *   <li>Sets up the cell factory for custom rendering of review items</li>
     * </ul>
     *
     * @param reviews The list of Review objects to display in the list view
     */
    private void setupReviewListView(List<Review> reviews) {
        if (reviews == null) {
            reviews = new ArrayList<>();
        }
        reviewObservableList.setAll(reviews);
        reviewsListView.setItems(reviewObservableList);
        reviewsListView.setCellFactory(listView -> new ReviewCell(this));
        reviewsListView.refresh();

        // Update the summary when reviews change
        updateReviewSummary(reviews);
    }

    /**
     * Handles saving an edited review when triggered by an action event.
     * <p>
     * This method delegates to the handleSaveEditedReview method that takes a Restaurant parameter,
     * retrieving the restaurant from the session first.
     * </p>
     * 
     * @param actionEvent The event that triggered this method
     */
    public void handleSaveEditedReview(ActionEvent actionEvent) {
        SessionService.getRestaurantFromSession().ifPresent(this::handleSaveEditedReview);
    }

    /**
     * Custom ListCell implementation for displaying reviews in the list view.
     * <p>
     * This inner class is responsible for rendering individual review items in the list view.
     * It displays the review content, user information, rating, and provides controls for
     * editing, deleting, and answering reviews based on the user's role.
     * </p>
     */
    public class ReviewCell extends ListCell<Review> {
        /**
         * Container for the review content including user information, rating, and text.
         */
        private final VBox contentBox;

        /**
         * Container for action buttons (edit, delete) shown for the user's own reviews.
         */
        private final HBox actionBox;

        /**
         * Label displaying the username of the review author.
         */
        private final Label userLabel;

        /**
         * Label displaying the star rating of the review.
         */
        private final Label starsLabel;

        /**
         * Label displaying the text content of the review.
         */
        private final Label contentLabel;

        /**
         * Container for the restaurant owner's answer to the review.
         */
        private final HBox answerBoxRow;

        /**
         * Label displaying the prefix text "Ristoratore: " before the answer.
         */
        private final Label answerLabelPrefix;

        /**
         * Label displaying the restaurant owner's answer to the review.
         */
        private final Label answerLabel;

        /**
         * Text field for entering a new answer to the review.
         */
        private final TextField answerField;

        /**
         * Container for the answer input components.
         */
        private final VBox answerBox;

        /**
         * Constructs a new ReviewCell with the specified controller.
         * <p>
         * This constructor initializes all UI components for displaying a review,
         * including the content area, action buttons, and answer components.
         * It also sets up event handlers for the edit, delete, and answer submission buttons.
         * </p>
         *
         * @param controller The RestaurantController that manages this cell
         */
        public ReviewCell(RestaurantController controller) {
            // Initialize components
            contentBox = new VBox(8); // 8px spacing
            contentBox.setPadding(new Insets(10));
            contentBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 5;");

            HBox headerBox = new HBox(10);

            userLabel = new Label();
            userLabel.getStyleClass().add("review-user");

            starsLabel = new Label();
            starsLabel.getStyleClass().add("review-stars");

            headerBox.getChildren().addAll(userLabel, starsLabel);

            contentLabel = new Label();
            contentLabel.setWrapText(true);
            contentLabel.getStyleClass().add("review-content");

            answerBoxRow = new HBox(5);
            answerLabelPrefix = new Label();
            answerLabelPrefix.setText("Ristoratore: ");
            answerLabelPrefix.setWrapText(true);
            answerLabelPrefix.getStyleClass().add("review-answer");
            answerLabelPrefix.setStyle("-fx-font-weight: bold;");
            answerLabel = new Label();
            answerLabel.setWrapText(true);
            answerLabel.getStyleClass().add("review-answer");

            // Initialize answer components
            answerField = new TextField();
            answerField.setPromptText("Rispondi al cliente...");
            answerField.setPrefWidth(340);

            Button answerButton = new Button("Submit");
            answerButton.setOnAction(e -> handleAnswerSubmit());

            answerBox = new VBox(5);
            answerBox.getChildren().addAll(answerField, answerButton);

            contentBox.getChildren().addAll(headerBox, contentLabel);

            // Create action buttons
            actionBox = new HBox(5);
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");

            editButton.getStyleClass().add("small-button");
            deleteButton.getStyleClass().add("small-button");

            actionBox.getChildren().addAll(editButton, deleteButton);
            actionBox.setVisible(false); // Only show for user's own reviews

            contentBox.getChildren().add(actionBox);

            // Add button handlers
            editButton.setOnAction(e -> controller.handleEditReview(getItem()));
            deleteButton.setOnAction(e -> controller.handleDeleteReview(SessionService.getRestaurantFromSession().orElse(null), getItem()));
        }

        @Override
        protected void updateItem(Review review, boolean empty) {
            super.updateItem(review, empty);

            if (empty || review == null) {
                setGraphic(null);
            } else {
                // Set user and rating
                userLabel.setText(review.getUser().getUsername());
                starsLabel.setText("★".repeat(Math.max(1, Math.min(5, review.getStars()))));

                // Set review content
                contentLabel.setText(review.getContent());

                // Handle answer display/input
                if (review.getAnswer() != null && !review.getAnswer().trim().isEmpty()) {
                    answerLabel.setText(review.getAnswer());
                    if (!contentBox.getChildren().contains(answerLabel)) {
                        answerBoxRow.getChildren().addAll(answerLabelPrefix, answerLabel);
                        contentBox.getChildren().add(answerBoxRow);
                    }
                    contentBox.getChildren().remove(answerBox);
                } else {
                    // Show answer input for restaurant owners
                    contentBox.getChildren().remove(answerBoxRow);
                    if (SessionService.getUserFromSession() != null && SessionService.getUserFromSession().getRole() == Role.RISTORATORE) {
                        if (!contentBox.getChildren().contains(answerBox)) {
                            contentBox.getChildren().add(answerBox);
                        }
                    } else {
                        contentBox.getChildren().remove(answerBox);
                    }
                }

                // Show edit/delete buttons only for the review author
                String currentUser = SessionService.getUserFromSession().getUsername();
                boolean isAuthor = review.getUser().getUsername().equals(currentUser);
                actionBox.setVisible(isAuthor);

                setGraphic(contentBox);
            }
        }

        /**
         * Handles the submission of an answer to a review.
         * <p>
         * This method is called when a restaurant owner submits an answer to a review.
         * It retrieves the current review, validates that the answer field is not empty,
         * updates the review with the answer, and persists the changes to the repository.
         * The UI is then updated to display the answer and the input field is cleared.
         * </p>
         */
        private void handleAnswerSubmit() {
            Review review = getItem();
            if (review != null && !answerField.getText().trim().isEmpty()) {
                review.setAnswer(answerField.getText().trim());
                updateItem(review, false);
                ReviewsRepository.editReview(review);
                // Clear the input field
                answerField.clear();
            }
        }
    }

    /**
     * Handles the edit review button click event.
     * <p>
     * This method prepares the UI for editing an existing review by:
     * </p>
     * <ul>
     *   <li>Storing the review being edited</li>
     *   <li>Populating the edit form with the review's content and rating</li>
     *   <li>Showing the edit form and hiding the add review form</li>
     * </ul>
     *
     * @param review The Review object to be edited
     */
    public void handleEditReview(Review review) {
        // Hide add review box and show edit box
        addReviewBox.setVisible(false);
        editReviewBox.setVisible(true);

        // Populate edit fields
        currentlyEditingReview = review;
        editReviewContent.setText(review.getContent());
        editRatingSelector.setValue(review.getStars());
    }

    /**
     * Handles the delete review button click event.
     * <p>
     * This method deletes a review from the repository and updates the UI.
     * It performs the following steps:
     * </p>
     * <ul>
     *   <li>Confirms with the user before deleting</li>
     *   <li>Removes the review from the repository</li>
     *   <li>Updates the restaurant's review list</li>
     *   <li>Refreshes the UI to reflect the changes</li>
     * </ul>
     *
     * @param restaurant The Restaurant object that contains the review
     * @param review The Review object to be deleted
     */
    public void handleDeleteReview(Restaurant restaurant, Review review) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Review");
        alert.setHeaderText("Delete Review");
        alert.setContentText("Are you sure you want to delete this review?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ReviewsRepository.ERROR_CODE result = ReviewsRepository.deleteReview(review);
                if (result == ReviewsRepository.ERROR_CODE.NONE) {
                    // Update the UI
                    restaurant.getReviews().remove(review);
                    setupReviewListView(restaurant.getReviews());
                } else {
                    AlertService.alert(Alert.AlertType.ERROR, "ATTENZIONE", null, "Impossibile eliminare la recensione");
                }
            }
        });
    }

    /**
     * Handles saving an edited review.
     * <p>
     * This method updates a review in the repository with the edited content and rating.
     * It performs the following steps:
     * </p>
     * <ul>
     *   <li>Validates that both content and rating are provided</li>
     *   <li>Updates the review object with the new values</li>
     *   <li>Saves the updated review to the repository</li>
     *   <li>Updates the UI to reflect the changes</li>
     *   <li>Hides the edit form and shows the add review form</li>
     * </ul>
     *
     * @param restaurant The Restaurant object that contains the review
     */
    @FXML
    public void handleSaveEditedReview(Restaurant restaurant) {
        if (currentlyEditingReview == null) return;

        String content = editReviewContent.getText();
        Integer rating = editRatingSelector.getValue();

        if (content == null || content.trim().isEmpty() || rating == null) {
            AlertService.alert(Alert.AlertType.WARNING, "ATTENZIONE", null, "Inserire tutti i campi richiesti per la modifica della recensione.");
            return;
        }

        // Update review
        currentlyEditingReview.setContent(content);
        currentlyEditingReview.setStars(rating);

        ReviewsRepository.ERROR_CODE result = ReviewsRepository.editReview(currentlyEditingReview);
        if (result == ReviewsRepository.ERROR_CODE.NONE) {
            // Reset UI
            editReviewBox.setVisible(false);
            addReviewBox.setVisible(true);
            currentlyEditingReview = null;

            // Refresh the list
            setupReviewListView(restaurant.getReviews());
        } else {
            AlertService.alert(Alert.AlertType.ERROR, "ATTENZIONE", null, "Impossibile modificare la recensione.");
        }
    }

    /**
     * Handles canceling the edit of a review.
     * <p>
     * This method cancels the current review editing operation by:
     * </p>
     * <ul>
     *   <li>Hiding the edit review form</li>
     *   <li>Showing the add review form</li>
     *   <li>Clearing the reference to the review being edited</li>
     * </ul>
     */
    @FXML
    public void handleCancelEdit() {
        editReviewBox.setVisible(false);
        addReviewBox.setVisible(true);
        currentlyEditingReview = null;
    }

    //#region Favorites

    @FXML
    public void handleToggleFavoriteRestaurant() {

        // Gets user & restaurant in session
        User user = validateUser();
        Restaurant restaurant = validateRestaurant();

        // Checks if they are in session + if user is not ristoratore
        if (user == null || restaurant == null || user.getRole() == Role.RISTORATORE) 
            return;

        // Toggle favorite in user object + alert on error
        UserRepository.ERROR_CODE result =  UserRepository.toggleFavoriteRestaurant(user, restaurant);
        if (result != UserRepository.ERROR_CODE.NONE) {
            AlertService.alert(Alert.AlertType.ERROR, "ATTENZIONE", null, "Errore durante l'aggiunta/rimozione del preferito.");
            return;
        }

        // If user now contains restaurant as favorite --> set text accordingly
        if (user.getRestaurants().contains(restaurant))
            toggleFavorite.setText("★");
        else
            toggleFavorite.setText("☆");

    }

    //#endregion

    //#region Restaurant CRUD

    /**
     * 
     */
    @FXML
    private void handleEditRestaurant() {

        Optional<Restaurant> optRestaurant = SessionService.getRestaurantFromSession();

        if (optRestaurant.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No restaurant selected to edit.");
            alert.showAndWait();
            return;
        }

        Restaurant restaurant = optRestaurant.get();

        if (restaurant == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No restaurant selected to edit.");
            alert.showAndWait();
            return;
        }

        // Create a dialog window with custom content
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Restaurant");
        dialog.setHeaderText("Modify the restaurant details");

        // Create fields pre-filled with current restaurant data
        TextField nameField = new TextField(restaurant.getName());
        TextField addressField = new TextField(restaurant.getAddress());
        TextField locationField = new TextField(restaurant.getLocation());
        TextField phoneField = new TextField(restaurant.getPhone());
        TextField cuisineField = new TextField(restaurant.getCuisine());
        TextField websiteField = new TextField(restaurant.getWebsiteUrl());

        TextField priceField = new TextField(restaurant.getPrice());
        TextField longitudeField = new TextField(restaurant.getLongitude() != null ? restaurant.getLongitude().toString() : "");
        TextField latitudeField = new TextField(restaurant.getLatitude() != null ? restaurant.getLatitude().toString() : "");
        TextField awardField = new TextField(restaurant.getAward());
        TextField greenStarField = new TextField(restaurant.getGreenStar() != null ? restaurant.getGreenStar().toString() : "");
        TextArea descriptionArea = new TextArea(restaurant.getDescription());
        descriptionArea.setPrefRowCount(4);

        // Layout the fields vertically
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
        grid.add(cuisineField, 1, 4);
        grid.add(new Label("Website URL:"), 0, 5);
        grid.add(websiteField, 1, 5);

        grid.add(new Label("Price:"), 0, 6);
        grid.add(priceField, 1, 6);
        grid.add(new Label("Longitude:"), 0, 7);
        grid.add(longitudeField, 1, 7);
        grid.add(new Label("Latitude:"), 0, 8);
        grid.add(latitudeField, 1, 8);
        grid.add(new Label("Award:"), 0, 9);
        grid.add(awardField, 1, 9);
        grid.add(new Label("Green Star (0 or 1):"), 0, 10);
        grid.add(greenStarField, 1, 10);
        grid.add(new Label("Description:"), 0, 11);
        grid.add(descriptionArea, 1, 11);

        dialog.getDialogPane().setContent(grid);

        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Show dialog and wait for result
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // --- VALIDAZIONI ---

                    // 1) Location: nessun numero consentito
                    if (locationField.getText().matches(".*\\d.*")) {
                        throw new IllegalArgumentException("Location cannot contain numbers.");
                    }

                    // 2) Latitudine: numero float tra -90 e 90
                    Float lat = null;
                    if (!latitudeField.getText().isBlank()) {
                        lat = Float.parseFloat(latitudeField.getText().trim());
                        if (lat < -90f || lat > 90f) {
                            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
                        }
                    }

                    // 3) Longitudine: numero float tra -180 e 180
                    Float lon = null;
                    if (!longitudeField.getText().isBlank()) {
                        lon = Float.parseFloat(longitudeField.getText().trim());
                        if (lon < -180f || lon > 180f) {
                            throw new IllegalArgumentException("Longitude must be between -180 and 180.");
                        }
                    }

                    // 4) Numero di telefono internazionale (esempio regex)
                    String phoneRegex = "^\\+?[0-9. ()-]{7,25}$";
                    if (!phoneField.getText().isBlank() && !phoneField.getText().matches(phoneRegex)) {
                        throw new IllegalArgumentException("Invalid international phone number format.");
                    }

                    // 5) URL sito web semplice (esempio regex per http(s)://...)
                    String urlRegex = "^(https?://)?([\\w.-]+)\\.([a-z]{2,6})([/\\w .-]*)*/?$";
                    if (!websiteField.getText().isBlank() && !websiteField.getText().matches(urlRegex)) {
                        throw new IllegalArgumentException("Invalid website URL format.");
                    }

                    // 6) Green Star: solo 0 o 1
                    Integer greenStar = null;
                    if (!greenStarField.getText().isBlank()) {
                        greenStar = Integer.parseInt(greenStarField.getText().trim());
                        if (greenStar != 0 && greenStar != 1) {
                            throw new IllegalArgumentException("Green Star must be 0 or 1.");
                        }
                    }

                    // --- SE ARRIVATO QUI, TUTTO OK ---

                    // Aggiorna i campi del ristorante
                    restaurant.setName(nameField.getText());
                    restaurant.setAddress(addressField.getText());
                    restaurant.setLocation(locationField.getText());
                    restaurant.setPhone(phoneField.getText());
                    restaurant.setCuisine(cuisineField.getText());
                    restaurant.setWebsiteUrl(websiteField.getText());
                    restaurant.setPrice(priceField.getText());
                    restaurant.setLongitude(lon);
                    restaurant.setLatitude(lat);
                    restaurant.setAward(awardField.getText());
                    restaurant.setGreenStar(greenStar);
                    restaurant.setDescription(descriptionArea.getText());

                    // Salva modifiche repository
                    RestaurantRepository.editRestaurant(restaurant);

                    // Aggiorna sessione e UI
                    SessionService.setRestaurantInSession(restaurant);
                    reloadRestaurantView();

                    Alert savedAlert = new Alert(Alert.AlertType.INFORMATION, "Restaurant information updated successfully!");
                    savedAlert.showAndWait();

                } catch (NumberFormatException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Invalid number format for longitude, latitude or green star.");
                    errorAlert.showAndWait();
                } catch (IllegalArgumentException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, e.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }



    
    /**
     * 
     */
    @FXML
    private void handleDeleteRestaurant(ActionEvent event) {

        Restaurant restaurant = validateRestaurant();

        if (restaurant == null) {
            // Nessun ristorante da eliminare, esci
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete restaurant");
        alert.setContentText("Are you sure you want to delete '" + restaurant.getName() + "'?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Prova a cancellare il ristorante
                RestaurantRepository.ERROR_CODE result = RestaurantRepository.deleteRestaurant(restaurant);
                if (result == RestaurantRepository.ERROR_CODE.NONE) {
                    // Cancellazione avvenuta con successo
                    // Torna indietro passando l'evento al metodo handleBack
                    SessionService.clearRestaurants();
                    handleBack();
                } else {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Could not delete restaurant");
                    errorAlert.setContentText("An error occurred while deleting the restaurant.");
                    errorAlert.showAndWait();
                }
            }
        });
    }



    /**
     * 
     */
    @FXML
    private void reloadRestaurantView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/restaurant.fxml"));
            // Ricarichi la scena dal file FXML
            SessionService.setSceneInSession(fxmlLoader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //#endregion
}
