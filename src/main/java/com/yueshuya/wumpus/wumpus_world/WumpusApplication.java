package com.yueshuya.wumpus.wumpus_world;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WumpusApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        WumpusController hc = new WumpusController(this);
        Scene rootScene = new Scene(hc.getAnchorPane(), 1024, 768);
        stage.setTitle("Wumpus Worls");
        stage.setScene(rootScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}