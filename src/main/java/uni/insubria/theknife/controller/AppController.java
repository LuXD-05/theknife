package uni.insubria.theknife.controller;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import uni.insubria.theknife.Main;

import java.io.IOException;

public class AppController {

    @FXML
    protected void onLoginButtonClick() throws IOException {
        //TODO validate user and password
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/home.fxml"));
        SessionController.setScene(fxmlLoader);
    }

    @FXML
    protected void onRegisterButtonClick() {}

    @FXML
    protected void onGuestButtonClick() {}
}