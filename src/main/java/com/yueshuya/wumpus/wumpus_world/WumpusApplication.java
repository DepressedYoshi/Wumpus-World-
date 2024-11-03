package com.yueshuya.wumpus.wumpus_world;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import static com.yueshuya.wumpus.wumpus_world.WumpusController.drawMap;

public class WumpusApplication extends Application {
    private final Player player = new Player();
    private final World world = new World(10, 10, this);  //;
    private WumpusController controller = new WumpusController(this);
    private AnimationTimer animationTimer;
    private boolean isBlind = true;
    private int score = 1000;
    private String gameState = "Hello, Welcome the Wumpus World, navigate with WASD and try to find the treasure. Good Luck! \n \n \n Press A for AI, Press R for Reset";

    //AI parts
    private AIPlayer aiPlayer = new AIPlayer(player, world);
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
            addScore(-1);
        }
        drawMap();  // Refresh the map after each move
    }

    public void checkGameState() {
        int tileValue = world.getRealPre();
        if (tileValue == World.TREASURE) {
            addScore(50);
            gameState = "You found the treasure! Now return to the start.";
            Player.setHasGold(true);
        } else if (tileValue == World.WUMPUS || tileValue == World.PIT || tileValue == World.SPIDER) {
            gameState ="Game Over: You encountered a hazard!";
            World.setGameover(true);
        } else if (Player.HasGold() && player.getCurrentLocation().equals(player.getStartLocation())) {
            gameState ="HOOORAY!!! YOU WIN";
            World.setGameover(true);
        }
        if (score < 1) {
            gameState ="Dude, stop, are you stupid.";
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
        player.reset();
        world.reset();
        World.setGameover(false);
        score = 1000;
        // Reset AI state if AI is enabled
        isAIControlled = false;
        aiPlayer.reset();
        gameState = "Press A for AI, Press R for Reset";
    }

    @Override
    public void start(Stage stage) throws IOException {
        Scene rootScene = new Scene(controller.getAnchorPane(), 1280, 800);
        stage.setTitle("Wumpus Worlds");
        stage.setScene(rootScene);

        rootScene.setOnKeyReleased(controller::handleKeyInput);
        if (animationTimer == null){
            animationTimer = new AnimationTimer() {

                @Override
                public void handle(long l) {

                        if(isAIControlled && !World.isGameover()){
                            aiPlayer.runAI();
                        }
                        checkGameState();
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
            aiPlayer = new AIPlayer(player,world); // Re-initialize AI
        } else {
            System.out.println("Manual Control Enabled");
        }
    }

    public int getScore() {
        return score;
    }
    public void addScore(int score){
        this.score += score;
    }

    public String getMessage(int i) {
        switch (i){
            case 1 -> {
                return gameState;
            }
            default -> {
                return "Hello, Welcome the Wumpus World, navigate with WASD and try to find the treasure. Good Luck! \n \n \n Press A for AI, Press R for Reset";
            }
        }
    }
}