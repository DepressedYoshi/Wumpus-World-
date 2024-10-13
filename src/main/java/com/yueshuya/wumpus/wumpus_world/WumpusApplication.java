package com.yueshuya.wumpus.wumpus_world;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static com.yueshuya.wumpus.wumpus_world.WumpusController.drawMap;

public class WumpusApplication extends Application {
    private Player player = new Player();
    private World world = new World(10, 10, this.player);  //;
    private AnimationTimer animationTimer;

    public Player getPlayer() {
        return player;
    }

    public World getWorld() {
        return world;
    }

    private void stopTimer() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    public void movePlayer(String direction) {
        if (player.move(direction, world)){
            checkGameState();  // Check if the player has encountered a hazard or treasure
        }
        drawMap();  // Refresh the map after each move
    }

    private void checkGameState() {
        int tileValue = world.PREVAL;
        if (tileValue == World.TREASURE) {
            System.out.println("You found the treasure! Now return to the start.");
            player.setHasGold(true);
        } else if (tileValue == World.WUMPUS || tileValue == World.PIT || tileValue == World.SPIDER) {
            System.out.println("Game Over: You encountered a hazard!");
            world.setGameover(true);

        }else if (player.HasGold() && player.getCurrentLocation().equals(player.getStartLocation())){
            System.out.println("WIN CONDITON MET");
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        WumpusController hc = new WumpusController(this);
        Scene rootScene = new Scene(hc.getAnchorPane(), 1280, 800);
        stage.setTitle("Wumpus Worlds");
        stage.setScene(rootScene);

        rootScene.setOnKeyReleased(hc::handleKeyInput);
        if (animationTimer == null){
            animationTimer = new AnimationTimer() {
                @Override
                public void handle(long l) {
                    drawMap();
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