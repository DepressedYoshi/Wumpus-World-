package com.yueshuya.wumpus.wumpus_world;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WumpusApplication extends Application {
    private AnimationTimer animationTimer;
    private void stopTimer() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
    @Override
    public void start(Stage stage) throws IOException {
        WumpusController hc = new WumpusController(this);
        Scene rootScene = new Scene(hc.getAnchorPane(), 1280, 800);
        stage.setTitle("Wumpus Worlds");
        stage.setScene(rootScene);
        if (animationTimer == null){
            animationTimer = new AnimationTimer() {
                @Override
                public void handle(long l) {
                    WumpusController.drawMap();
                }
            };
            animationTimer.start();
        }
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}