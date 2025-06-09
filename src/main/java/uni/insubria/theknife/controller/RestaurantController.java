package uni.insubria.theknife.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.Review;
import uni.insubria.theknife.model.Role;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.repository.ReviewsRepository;
import uni.insubria.theknife.service.SessionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;

public class RestaurantController {
    //TODO
    //GITHUB TASK #12 - #13 - #14
    //Risposta recensioni + Report riepilogo recensioni + dettaglio recensioni

    private static final String LOGIN_PROMPT = "Please login to add a review";
    private static final String MISSING_INFO_TITLE = "Missing Information";
    private static final String REVIEW_VALIDATION_MESSAGE = "Please provide both a review and a rating.";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found.";
    private static final String RESTAURANT_NOT_FOUND_MESSAGE = "Restaurant not found.";

    @FXML
    private Label nameLabel, descriptionLabel, addressLabel, priceLabel;
    @FXML
    private Label cuisineLabel, longitudeLabel, latitudeLabel, phoneLabel;
    @FXML
    private Label michelinUrlLabel, websiteUrlLabel, awardLabel;
    @FXML
    private Label greenStarLabel, facilitiesLabel, registerMessage;
    @FXML
    private Button addReview;
    @FXML
    private VBox addReviewBox;
    @FXML
    private ComboBox<String> ratingSelector;
    @FXML
    private TextField reviewContent;
    @FXML
    private Label welcomeLabel;
    @FXML
    private VBox replyBox;
    @FXML
    private TextField replyContent;
    @FXML
    private ListView<Review> reviewsListView;

    private final ObservableList<Review> reviewObservableList = FXCollections.observableArrayList();

    private Review selectedReview;

    @FXML
    private void initialize() {
        initializeUserState();
        initializeRatingSelector();
        populateRestaurantDetails();
    }

    private void initializeReviewsList(Restaurant restaurant) {
        if (restaurant.getReviews() == null) {
            restaurant.setReviews(new ArrayList<>());
        }
        setupReviewListView(restaurant.getReviews());
        reviewsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && SessionService.getUserFromSession() != null
                    && SessionService.getUserFromSession().getRole() == Role.RISTORATORE) {
                selectedReview = newVal;
                replyBox.setVisible(true);
                replyContent.setText(newVal.getAnswer() != null ? newVal.getAnswer() : "");
            }
        });
    }

    private void initializeUserState() {
        User user = SessionService.getUserFromSession();
        welcomeLabel.setText(String.format("Welcome %s!", user != null ? user.getUsername() : "guest"));

        replyBox.setVisible(false); // Initially hide reply box
        boolean isRistoratore = user != null && user.getRole() == Role.RISTORATORE;
        if (isRistoratore) {
            addReviewBox.setVisible(true);
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

    private void initializeRatingSelector() {
        if (ratingSelector != null) {
            ratingSelector.setItems(FXCollections.observableArrayList(
                    "1 star", "2 stars", "3 stars", "4 stars", "5 stars"
            ));
        }
    }

    private void populateRestaurantDetails() {
        Optional<Restaurant> optionalRestaurant = SessionService.getRestaurantFromSession();
        optionalRestaurant.ifPresent(this::displayRestaurantInfo);
    }

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
        boolean hasExistingReview = restaurant.getReviews().stream()
                .anyMatch(review -> review.getUser().getUsername().equals(user.getUsername()));

        if (hasExistingReview) {
            showAlert(MISSING_INFO_TITLE, "You have already reviewed this restaurant.");
            return;
        }

        Review newReview = createReview(user, restaurant);

        // Add review to restaurant's reviews list
        restaurant.getReviews().add(newReview);

        // Add review to user's reviews list
        user.getReviews().add(newReview);

        // Update the ListView
        setupReviewListView(restaurant.getReviews());

        clearReviewInputs();
    }

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

    private void setLabelText(Label label, String text) {
        if (label != null) {
            label.setText(text != null ? text : "");
        }
    }


    private boolean validateReviewInput() {
        return !reviewContent.getText().isEmpty() && ratingSelector.getValue() != null;
    }

    private User validateUser() {
        User user = SessionService.getUserFromSession();
        if (user == null) {
            showAlert(MISSING_INFO_TITLE, USER_NOT_FOUND_MESSAGE);
        }
        return user;
    }

    private Restaurant validateRestaurant() {
        Restaurant restaurant = SessionService.getRestaurantFromSession().orElse(null);
        if (restaurant == null) {
            showAlert(MISSING_INFO_TITLE, RESTAURANT_NOT_FOUND_MESSAGE);
        }
        return restaurant;
    }

    private Review createReview(User user, Restaurant restaurant) {
        String ratingText = ratingSelector.getValue();
        int stars = Character.getNumericValue(ratingText.charAt(0));
        return new Review()
                .setUser(user)
                .setRestaurant(restaurant)
                .setContent(reviewContent.getText())
                .setStars(stars);
    }

    private void clearReviewInputs() {
        reviewContent.clear();
        ratingSelector.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleAddReply() {
        if (selectedReview == null || replyContent.getText().isEmpty()) {
            showAlert(MISSING_INFO_TITLE, "Please select a review and write a reply.");
            return;
        }

        selectedReview.setAnswer(replyContent.getText());
        reviewsListView.refresh();

        replyContent.clear();
        replyBox.setVisible(false);
        selectedReview = null;
        reviewsListView.getSelectionModel().clearSelection();
    }


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

private void setupReviewListView(List<Review> reviews) {
    if (reviews == null) {
        reviews = new ArrayList<>();
    }
    reviewObservableList.setAll(reviews);
    reviewsListView.setItems(reviewObservableList);
    reviewsListView.setCellFactory(listView -> new ReviewCell());
    reviewsListView.refresh();
}

private static class ReviewCell extends ListCell<Review> {
    private final VBox contentBox;
    private final Label userLabel;
    private final Label starsLabel;
    private final Label contentLabel;
    private final Label answerLabel;
    private final TextField answerField;
    private final VBox answerBox;

    public ReviewCell() {
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

        answerLabel = new Label();
        answerLabel.setWrapText(true);
        answerLabel.getStyleClass().add("review-answer");

        // Initialize answer components
        answerField = new TextField();
        answerField.setPromptText("Write your answer...");
        answerField.setPrefWidth(340);

        Button answerButton = new Button("Submit");
        answerButton.setOnAction(e -> handleAnswerSubmit());

        answerBox = new VBox(5);
        answerBox.getChildren().addAll(answerField, answerButton);

        contentBox.getChildren().addAll(headerBox, contentLabel);
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
                // Show existing answer
                answerLabel.setText("Response: " + review.getAnswer());
                if (!contentBox.getChildren().contains(answerLabel)) {
                    contentBox.getChildren().add(answerLabel);
                }
                contentBox.getChildren().remove(answerBox);
            } else {
                // Show answer input for restaurant owners
                contentBox.getChildren().remove(answerLabel);
                if (SessionService.getUserFromSession() != null &&
                        SessionService.getUserFromSession().getRole() == Role.RISTORATORE) {
                    if (!contentBox.getChildren().contains(answerBox)) {
                        contentBox.getChildren().add(answerBox);
                    }
                } else {
                    contentBox.getChildren().remove(answerBox);
                }
            }

            setGraphic(contentBox);
        }
    }

    private void handleAnswerSubmit() {
        Review review = getItem();
        if (review != null && !answerField.getText().trim().isEmpty()) {
            review.setAnswer(answerField.getText().trim());
            updateItem(review, false);
            // Clear the input field
            answerField.clear();
        }
    }
}
}