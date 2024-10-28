package com.yueshuya.wumpus.wumpus_world;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.nio.file.attribute.FileAttribute;

import static com.yueshuya.wumpus.wumpus_world.WumpusController.drawMap;

public class WumpusApplication extends Application {
    private final Player player = new Player();
    private final World world = new World(10, 10, this.player);  //;
    private AnimationTimer animationTimer;
    private boolean isBlind = true;
    private int score = 1000;

    //AI parts
    private AIPlayer aiPlayer;
    private boolean isAIControlled = false;

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
        int tileValue = world.getRealPre();
        if (tileValue == World.TREASURE) {
            System.out.println("You found the treasure! Now return to the start.");
            Player.setHasGold(true);
        } else if (tileValue == World.WUMPUS || tileValue == World.PIT || tileValue == World.SPIDER) {
            System.out.println("Game Over: You encountered a hazard!");
            World.setGameover(true);

        }else if (Player.HasGold() && player.getCurrentLocation().equals(player.getStartLocation())){
            System.out.println("WIN CONDITON MET");
        }
        if (score < 1 ){
            System.out.println("Dude, stop, are your stupid.");
        }
    }

    public void toggleFog() {isBlind = !isBlind;
        if (!isBlind){
            world.clearAllFog();
        }else{
            world.genFogOfWar();
        }
        drawMap();
    }

    public void reset() {
        world.clear();
        player.reset();
        world.reset();
        World.setGameover(false);
        score = 1000;
        // Reset AI state if AI is enabled
        isAIControlled = false;
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
                    if(isAIControlled && !World.isGameover()){
                        aiPlayer.runAI();
                    }
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

    protected void toggleAIControl() {
        isAIControlled = !isAIControlled;
        if (isAIControlled) {
            System.out.println("AI Control Enabled");
            aiPlayer = new AIPlayer(player, world); // Re-initialize AI
        } else {
            System.out.println("Manual Control Enabled");
        }
    }

}