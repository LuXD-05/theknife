package uni.insubria.theknife.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uni.insubria.theknife.model.User;
import java.io.IOException;
import java.util.HashMap;

public class SessionController {
    private static final HashMap<String, Object> session = new HashMap<>();

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
