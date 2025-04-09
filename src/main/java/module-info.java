module uni.insubria.theknife {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires com.opencsv;
    requires com.fasterxml.jackson.databind;
    requires java.sql;

    opens uni.insubria.theknife.controller to javafx.fxml;
    
    exports uni.insubria.theknife.model to com.opencsv;
    exports uni.insubria.theknife;
}