package uni.insubria.theknife.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.Review;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.service.SessionService;

import java.io.IOException;
import java.util.Optional;

public class RestaurantController {
    public Button filterButton;
    public GridPane restaurantGridPane;
    @FXML
    public Label nameLabel;
    @FXML
    public Label descriptionLabel;
    @FXML
    public Label addressLabel;
    @FXML
    public Label priceLabel;
    @FXML
    public Label cuisineLabel;
    @FXML
    public Label longitudeLabel;
    @FXML
    public Label latitudeLabel;
    @FXML
    public Label phoneLabel;
    @FXML
    public Label michelinUrlLabel;
    @FXML
    public Label websiteUrlLabel;
    @FXML
    public Label awardLabel;
    @FXML
    public Label greenStarLabel;
    @FXML
    public Label facilitiesLabel;
    @FXML
    public Button backButton;
    @FXML
    public ScrollPane reviewScrollPane;
    @FXML
    public VBox reviewsVBox;
    @FXML
    public Button addReview;

    ObservableList<Review> reviewObservableList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        Optional<Restaurant> optionalRestaurant = SessionService.getRestaurantFromSession();
        if (optionalRestaurant.isPresent()) {
            Restaurant restaurant = optionalRestaurant.get();
            nameLabel.setText(restaurant.getName() != null ? restaurant.getName() : "");
            descriptionLabel.setText(restaurant.getDescription() != null ? restaurant.getDescription() : "");
            addressLabel.setText(restaurant.getAddress() != null ? restaurant.getAddress() : "");
            priceLabel.setText(restaurant.getPrice() != null ? restaurant.getPrice() : "");
            cuisineLabel.setText(restaurant.getCuisine() != null ? restaurant.getCuisine() : "");
            longitudeLabel.setText(restaurant.getLongitude() != 0 ? String.valueOf(restaurant.getLongitude()) : "");
            latitudeLabel.setText(restaurant.getLatitude() != 0 ? String.valueOf(restaurant.getLatitude()) : "");
            phoneLabel.setText(restaurant.getPhone() != null ? restaurant.getPhone() : "");
            michelinUrlLabel.setText(restaurant.getMichelinUrl() != null ? restaurant.getMichelinUrl() : "");
            websiteUrlLabel.setText(restaurant.getWebsiteUrl() != null ? restaurant.getWebsiteUrl() : "");
            awardLabel.setText(restaurant.getAward() != null ? restaurant.getAward() : "");
            greenStarLabel.setText(restaurant.getGreenStar() != 0 ? String.valueOf(restaurant.getGreenStar()) : "");
            facilitiesLabel.setText(restaurant.getFacilities() != null ? restaurant.getFacilities() : "");
            reviewObservableList = FXCollections.observableArrayList(restaurant.getReviews());
        }
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
    private void handleAddReview() {
        Review newReview = new Review();
        // Get the user from the session
        User user = SessionService.getUserFromSession();
        newReview.setUser(user);

        // Assign the restaurant to the review
        Restaurant restaurant = SessionService.getRestaurantFromSession().get();
        newReview.setRestaurant(restaurant);

        // Set review content and stars here, you can use text input to get these value from user
        // for example:
        // newReview.setContent(reviewContentInput.getText());
        // newReview.setStars(Integer.parseInt(reviewStarsInput.getText()));

        restaurant.getReviews().add(newReview);

        reviewObservableList.add(newReview);
    }
}
