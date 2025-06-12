package uni.insubria.theknife.service;

import javafx.scene.control.Alert;
import lombok.NonNull;

/**
 * Service for displaying alert dialogs in the TheKnife application.
 * <p>
 * This utility class provides a simplified interface for creating and showing
 * JavaFX alert dialogs with customizable content and appearance.
 * </p>
 * <p>
 * Alerts can be used to display information, warnings, errors, or confirmation
 * messages to the user.
 * </p>
 */
public class AlertService {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AlertService() {
        // This constructor is not meant to be called
    }
    /**
     * Displays an alert dialog with the specified parameters.
     *
     * @param level   The type of alert to display (e.g., INFORMATION, WARNING, ERROR)
     * @param title   The title text for the alert dialog
     * @param header  The header text for the alert dialog (can be null)
     * @param content The content text for the alert dialog
     */
    public static void alert(@NonNull Alert.AlertType level, String title, String header, String content) {
        Alert alert = new Alert(level);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
