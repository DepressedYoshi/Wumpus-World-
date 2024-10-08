package com.yueshuya.wumpus.wumpus_world;
import javafx.geometry.Point2D;
import javafx.scene.layout.AnchorPane;



public class WumpusController {
    private WumpusApplication app;
    private final AnchorPane anchorPane;
    private World world;
    private Player player;

    public WumpusController(WumpusApplication app){
        this.app = app;
        anchorPane = new AnchorPane();
        createGui();
        attachListeners();
        world = new World(10, 10);  // Create a 10x10 grid world
        player = new Player(new Point2D(9, 0));  // Player starts at bottom-left (9,0)
        world.populateWorld();  // Randomly place hazards and treasure

    }

    private void createGui(){

    }
    private void attachListeners(){

    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }


    public void movePlayer(String direction) {
        if (player.move(direction, world)) {
            // Check if the player has reached treasure or hazard
            checkGameState();
        }
    }

    private void checkGameState() {
        Point2D playerLocation = player.getCurrentLocation();
        int tileValue = world.getTile(playerLocation);
        if (tileValue == World.TREASURE) {
            System.out.println("You found the treasure! Now return to the start.");
        } else if (tileValue == World.WUMPUS || tileValue == World.PIT || tileValue == World.SPIDER) {
            System.out.println("Game Over: You encountered a hazard!");
        }
    }

}