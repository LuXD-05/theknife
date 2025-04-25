package uni.insubria.theknife.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import uni.insubria.theknife.model.Restaurant;
import uni.insubria.theknife.model.User;
import uni.insubria.theknife.repository.RestaurantRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SessionService {
    private static final HashMap<String, Object> session = new HashMap<>();
    @Getter
    private static final List<Restaurant> restaurants = RestaurantRepository.loadRestaurants();
    @Getter
    private static final List<String> locations = restaurants.stream().map(Restaurant::getLocation).collect(Collectors.toSet()).stream().sorted().toList();

    public static void setStage(Stage stage, FXMLLoader fxmlLoader) throws IOException {
        session.put("stage", stage);
        setScene(fxmlLoader);
        stage.show();
    }

    public static void setScene(FXMLLoader fxmlLoader) throws IOException {
        Scene scene = new Scene(fxmlLoader.load());
        ((Stage) session.get("stage")).setScene(scene);
    }

    public static User getUser() {
        return ((User) session.get("user"));
    }

    public static void setUser(User user) {
        session.put("user", user);
    }
}
