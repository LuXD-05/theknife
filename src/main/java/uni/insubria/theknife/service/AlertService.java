package uni.insubria.theknife.service;

import javafx.scene.control.Alert;
import lombok.NonNull;

public class AlertService {
    public static void alert(@NonNull Alert.AlertType level, String title, String header, String content) {
        Alert alert = new Alert(level);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
