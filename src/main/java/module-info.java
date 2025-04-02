module uni.insubria.theknife {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires com.opencsv;

    opens uni.insubria.theknife.controller to javafx.fxml;
    exports uni.insubria.theknife;
}