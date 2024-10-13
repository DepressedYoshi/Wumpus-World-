package com.yueshuya.wumpus.wumpus_world;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;


public class WumpusController {
    private WumpusApplication app;
    private final AnchorPane anchorPane;
    private final Player player;
    private static World world;
    private static GraphicsContext gc;
    private Canvas canvas;

    private Button pitButton;
    private Button spiderButton;
    private Button wumpusButton;
    private Button chestButton;
    private Button groundButton;
    private Button startButton;
    private Button upButton;
    private Button downButton;
    private Button leftButton;
    private Button rightButton;

    public WumpusController(WumpusApplication app){
        this.app = app;
        anchorPane = new AnchorPane();
        anchorPane.setStyle("-fx-background-color: black;");
        canvas = new Canvas(800,800);
        player = app.getPlayer();  // Player starts at bottom-left (9,0)
        world = app.getWorld();
        createGui();
        attachListeners();
    }

    private void createGui(){
        createCanvas();
        createButtons();
    }

    private void createCanvas() {
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800,800);
        AnchorPane.setLeftAnchor(canvas, 20.0);
        AnchorPane.setTopAnchor(canvas, 20.0);
        anchorPane.getChildren().add(canvas);
    }



    private void createButtons() {
        pitButton = createButton(130.0, 200.0, world.getPitTile());
        spiderButton = createButton(190.0, 200.0, world.getSpidertile());
        wumpusButton = createButton(250.0, 200.0, world.getWumpustile());
        chestButton = createButton(310.0, 200.0, world.getGoldTile());
        groundButton = createButton(370.0, 200.0, world.getGroundTile());
        startButton = createButton(430, 200.0, world.getQuestion());
        upButton = createButton(510, 200.0, world.getUpArrow());
        downButton = createButton(570, 200.0, world.getDownArrow());
        leftButton = createButton(570, 270.0,world.getLeftArrow());
        rightButton = createButton(570, 130,world.getRightArrow());

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

    private void attachListeners(){
        pitButton.setOnAction(this::handleButtonClick);
        spiderButton.setOnAction(this::handleButtonClick);
        wumpusButton.setOnAction(this::handleButtonClick);
        chestButton.setOnAction(this::handleButtonClick);
        groundButton.setOnAction(this::handleButtonClick);
        startButton.setOnAction(this::handleButtonClick);

        rightButton.setOnAction(e -> app.movePlayer("right"));
        upButton.setOnAction(e -> app.movePlayer("up"));
        downButton.setOnAction(e -> app.movePlayer("down"));
        leftButton.setOnAction(e -> app.movePlayer("left"));
    }

    protected void handleKeyInput(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case SPACE:
                toggleFogOfWar();
                break;
            case UP:
                app.movePlayer("up");
                break;
            case DOWN:
                app.movePlayer("down");
                break;
            case LEFT:
                app.movePlayer("left");
                break;
            case RIGHT:
                app.movePlayer("right");
                break;
            default:
                break;
        }
    }

    private void toggleFogOfWar() {
    }

    private void handleButtonClick(ActionEvent actionEvent) {
        if (actionEvent.getSource() == pitButton){
            handleSetup();
        }if (actionEvent.getSource() == spiderButton){
            handleSetup();
        }if (actionEvent.getSource() == wumpusButton){
            handleSetup();
        }if (actionEvent.getSource() == chestButton){
            handleSetup();
        }

    }

    private void handleSetup() {
    }

    public AnchorPane getAnchorPane() {
        return anchorPane;
    }

    public static void drawMap() {
        final int XOFFSET = 150;
        final int YOFFSET = 120;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                gc.drawImage(world.getGroundTile().getImage(),XOFFSET+j*World.SIZE, YOFFSET+i*World.SIZE);
                if (world.getTile(new Point2D(j,i)) >0){
                    ImageView imageView = world.getImage(j,i);
                    if (imageView!= null && imageView.getImage() !=null){
                        gc.drawImage(imageView.getImage(), XOFFSET+j*World.SIZE, YOFFSET+i*World.SIZE);
                    }
                }
            }
        }
    }
}