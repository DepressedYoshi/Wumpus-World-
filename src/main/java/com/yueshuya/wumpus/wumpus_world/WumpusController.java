package com.yueshuya.wumpus.wumpus_world;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;


public class WumpusController {
    private WumpusApplication app;
    private final AnchorPane anchorPane;
    private Canvas canvas;
    private static World world;
    private Player player;
    private Button breez;
    private Button web;
    private Button stench;
    private Button glitter;
    private Button treasure;
    private Button empty;
    private Button start;
    private static GraphicsContext gc;

    public WumpusController(WumpusApplication app){
        this.app = app;
        anchorPane = new AnchorPane();
        anchorPane.setStyle("-fx-background-color: black;");
        player = new Player(new Point2D(9, 0));  // Player starts at bottom-left (9,0)
        world = new World(10, 10, player);  // Create a 10x10 grid world
        createGui();
        attachListeners();
    }



    private void createGui(){
        createCanvas();
        createButtons();
    }




    private void createButtons() {
        breez = createButton(130.0, 140.0, world.getPitTile());
        web = createButton(190.0, 140.0, world.getSpidertile());
        stench = createButton(250.0, 140.0, world.getWumpustile());
        treasure = createButton(310.0, 140, world.getGoldTile());
        empty = createButton(370.0, 140,world.getGroundTile());
        start = createButton(560, 140, world.getRightArrow());

    }
    private Button createButton(double topAnchor, double rightAnchor, ImageView imageView) {
        Button button = new Button();
        button.setGraphic(imageView);
        button.setPrefWidth(50);
        AnchorPane.setTopAnchor(button, topAnchor);
        AnchorPane.setRightAnchor(button, rightAnchor);
        anchorPane.getChildren().add(button);
        return button;
    }

    private void createCanvas() {
        Canvas canvas = new Canvas(800,800);
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800,800);
        AnchorPane.setLeftAnchor(canvas, 20.0);
        AnchorPane.setTopAnchor(canvas, 20.0);
        anchorPane.getChildren().add(canvas);
    }

    private void attachListeners(){
//        breez.setOnAction(this::handleButtonClick);
//        web.setOnAction(this::handleButtonClick);
//        stench.setOnAction(this::handleButtonClick);
//        glitter.setOnAction(this::handleButtonClick);
//        treasure.setOnAction(this::handleButtonClick);
//        empty.setOnAction(this::handleButtonClick);
//        start.setOnAction(this::handleButtonClick);
    }

    private void handleButtonClick(ActionEvent actionEvent) {

    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public static void drawMap() {

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                gc.drawImage(world.getGroundTile().getImage(),j*World.SIZE, i*World.SIZE);
                if (world.getTile(new Point2D(i,j)) >0){
                    ImageView imageView = world.getImage(i,j);
                    if (imageView!= null && imageView.getImage() !=null){
                        gc.drawImage(imageView.getImage(), j*World.SIZE, i*World.SIZE);
                    }
                }
            }
        }
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