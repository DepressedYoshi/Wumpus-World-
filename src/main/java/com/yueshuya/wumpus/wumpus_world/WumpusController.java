package com.yueshuya.wumpus.wumpus_world;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.Objects;


public class WumpusController {
    private static WumpusApplication app;
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

    private Label bannerLabel;
    private static Label scoreLabel;
    private static Label messageArea;

    Font titleFont = Font.loadFont(getClass().getResourceAsStream("/lib/Stanford_Breath.ttf"), 48);
    Font messageFont = Font.loadFont(getClass().getResourceAsStream("/lib/Morsan.ttf"), 24);


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
        createText();
    }

    private void createText() {
        // Initialize banner and message area
        bannerLabel = new Label("Wumpus World");
        bannerLabel.setFont(titleFont);
        bannerLabel.setTextFill(Color.WHITE);
        bannerLabel.setTextAlignment(TextAlignment.CENTER);
        bannerLabel.setAlignment(Pos.CENTER);


        scoreLabel = new Label("Score: " + app.getScore());
        scoreLabel.setFont(messageFont);
        scoreLabel.setTextFill(Color.WHITE);

        messageArea = new Label(app.getMessage(0));
        messageArea.setFont(messageFont);
        messageArea.setTextFill(Color.WHITE);
        messageArea.setTextAlignment(TextAlignment.CENTER);
        messageArea.setWrapText(true);

        // Set layout positions for banner and message area
        AnchorPane.setTopAnchor(bannerLabel, 10.0);
        AnchorPane.setLeftAnchor(bannerLabel, 10.0);
        AnchorPane.setRightAnchor(bannerLabel, 10.0);

        AnchorPane.setBottomAnchor(scoreLabel, 100.0);
        AnchorPane.setRightAnchor(scoreLabel, 100.0);

        AnchorPane.setTopAnchor(messageArea, -100.0);
        AnchorPane.setRightAnchor(messageArea, 20.0);
        messageArea.setPrefSize(300, 600);

        // Add banner and message area to the AnchorPane
        anchorPane.getChildren().addAll(bannerLabel, messageArea, scoreLabel);

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
        pitButton = createButton(130.0, 450.0, world.getPitTile());
        spiderButton = createButton(190.0, 450.0, world.getSpidertile());
        wumpusButton = createButton(250.0, 450.0, world.getWumpustile());
        chestButton = createButton(310.0, 450.0, world.getGoldTile());
        groundButton = createButton(370.0, 450.0, world.getGroundTile());
        startButton = createButton(430.0, 450.0, world.getQuestion());
        upButton = createButton(520.0, 450.0, world.getUpArrow());
        downButton = createButton(580.0, 450.0, world.getDownArrow());
        leftButton = createButton(580.0, 520.0,world.getLeftArrow());
        rightButton = createButton(580.0, 380.0,world.getRightArrow());

    }
    private Button createButton(double topAnchor, double rightAnchor, ImageView imageView) {
        Button button = new Button();
        button.setGraphic(imageView);
        button.setPrefWidth(50);
        AnchorPane.setTopAnchor(button, topAnchor);
        AnchorPane.setRightAnchor(button, rightAnchor);
        anchorPane.getChildren().add(button);
        button.setFocusTraversable(false);
        return button;
    }

    private void attachListeners(){
        pitButton.setOnAction(this::handleButtonClick);
        spiderButton.setOnAction(this::handleButtonClick);
        wumpusButton.setOnAction(this::handleButtonClick);
        chestButton.setOnAction(this::handleButtonClick);
        groundButton.setOnAction(this::handleButtonClick);
        startButton.setOnAction(e -> app.toggleAIControl());

        rightButton.setOnAction(e -> app.movePlayer("right"));
        upButton.setOnAction(e -> app.movePlayer("up"));
        downButton.setOnAction(e -> app.movePlayer("down"));
        leftButton.setOnAction(e -> app.movePlayer("left"));
    }

    protected void handleKeyInput(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
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
            case R:
                app.reset();
                break;
            case SPACE:
                app.toggleFog();
                break;
            case A:
                app.toggleAIControl();
            default:
                break;
        }
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
                if (world.getTile(new Point2D(j,i)) > 0){
                    if (world.getRealPre() != 0){
                        gc.drawImage(world.getRealBackground().getImage(),XOFFSET+j*World.SIZE, YOFFSET+i*World.SIZE);
                    }
                    ImageView imageView = world.getImage(world.getTile(new Point2D(j, i)));
                    if (imageView!= null && imageView.getImage() !=null){
                        gc.drawImage(imageView.getImage(), XOFFSET+j*World.SIZE, YOFFSET+i*World.SIZE);
                    }
                }
            }
        }
        updateText();
    }

    private static void updateText() {
        scoreLabel.setText("Score: " + app.getScore());
        messageArea.setText(app.getMessage(1));
    }
}