package uni.insubria.theknife.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import uni.insubria.theknife.service.SessionService;

public class HomeController {
    @FXML
    Label welcomeLabel;

    @FXML
    private void initialize() {
        if (SessionService.getUser() != null) {
            welcomeLabel.setText("Welcome " + SessionService.getUser().getFirstName() + "!");
        }
    }
}
