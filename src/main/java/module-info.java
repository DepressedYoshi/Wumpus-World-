module com.yueshuya.wumpus.wumpus_world {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.yueshuya.wumpus.wumpus_world to javafx.fxml;
    exports com.yueshuya.wumpus.wumpus_world;
}