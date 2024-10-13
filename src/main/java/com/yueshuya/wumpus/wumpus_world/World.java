package com.yueshuya.wumpus.wumpus_world;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Random;

public class World {
    public static final int EMPTY = 0;
    public static final int WUMPUS = 1;
    public static final int PIT = 2;
    public static final int SPIDER = 3;
    public static final int TREASURE = 4;
    public static final int WEB = 5;
    public static final int BREEZ = 6;
    public static final int STINK = 7;
    public static final int GLITTER = 8;
    public static final int PLAYER = 9;
    public static final int SIZE=50;
    private static int PREVAL = 0;

    private final int[][] neighbor = {
            {-1,0}, {0,1}, {1,0},{0,-1}, {-1,-1,},{-1,1},{1,1},{1,-1}
    };
    private int[][] grid;
    private Random random;

    private ImageView blackTile = loadImage("/lib/blackTile.png");
    private ImageView downArrow = loadImage("/lib/downArrow.png");
    private ImageView emptyChest = loadImage("/lib/emptyChest.png");
    private ImageView glitterTile = loadImage("/lib/glitterTile.png");
    private ImageView goldTile = loadImage("/lib/goldTile.png");
    private ImageView groundTile = loadImage("/lib/groundTile.png");
    private ImageView guy = loadImage("/lib/guy.png");
    private ImageView leftArrow = loadImage("/lib/leftArrow.png");
    private ImageView pitTile = loadImage("/lib/pitTile.png");
    private ImageView plus = loadImage("/lib/plus.png");
    private ImageView question = loadImage("/lib/question.png");
    private ImageView rightArrow = loadImage("/lib/rightArrow.png");
    private ImageView spidertile = loadImage("/lib/spiderTile.png");
    private ImageView stinkTtie = loadImage("/lib/stinkTile.png");
    private ImageView trophy = loadImage("/lib/trophy.png");
    private ImageView upArrow = loadImage("/lib/upArrow.png");
    private ImageView webTile = loadImage("/lib/webTile.png");
    private ImageView windTile = loadImage("/lib/windTile.png");
    private ImageView wumpustile = loadImage("/lib/wumpusTile.png");

    public World(int rows, int cols, Player player) {
        grid = new int[rows][cols];
        random = new Random();
        placePlayer(player);
        populateWorld();
        printGrid();
    }

    private void placePlayer(Player player) {
        int row = (int) player.getCurrentLocation().getY();
        int col = (int) player.getCurrentLocation().getX();
        grid[row][col] = PLAYER;
    }

    private void populateWorld() {
        placeTreasure(); // Place the treasure first
        genHazrd(WUMPUS, 1); // Then generate the hazards
        genHazrd(PIT, random.nextInt(4) + 2);
        genHazrd(SPIDER, random.nextInt(2) + 1);
    }

    private void placeTreasure() {
        int row, col;
        do {
            row = random.nextInt(grid.length);
            col = random.nextInt(grid[0].length);
        } while (grid[row][col] != EMPTY || !notClose(row, col));

        grid[row][col] = TREASURE;
        for (int i = 0; i < 4; i++) {
            int newrow = row+neighbor[i][0];
            int newcol = col+neighbor[i][1];
            if (isValidIndex(new Point2D(newrow, newcol)))
                grid[newrow][newcol] = GLITTER;
        }
    }



    private void genHazrd(int hazard, int count) {
        while (count > 0) {
            int row = random.nextInt(grid.length);
            int col = random.nextInt(grid[0].length);
            if (notClose(row, col)){
                placeHazard(hazard, row, col);
                count--;
            }
        }
    }
    private void placeHazard(int hazard, int row, int col) {
            if (grid[row][col] == EMPTY) {
                grid[row][col] = hazard;
                for (int i = 0; i < 4; i++) {
                    int newrow = row+neighbor[i][0];
                    int newcol = col+neighbor[i][1];
                    if (isValidIndex(new Point2D(newrow, newcol)))
                        grid[newrow][newcol] = getSensory(hazard);
                }
            }
    }

    private int getSensory(int hazard) {
        switch (hazard){
            case WUMPUS :
                return STINK;
            case PIT:
                return BREEZ;
            case SPIDER:
                return WEB;
            case TREASURE:
                return GLITTER;
            default:
                return EMPTY;
        }
    }

    private boolean notClose(int row, int col) {
        // Ensure that hazards are not placed too close to the player
        if (grid[row][col] != EMPTY) {
            return false; // Do not place hazard at the player's location
        }

        for(int[] n : neighbor) {
            Point2D location = new Point2D(row + n[1], col + n[0]);
            if (isValidIndex(location)) {
                if (getTile(location) != EMPTY) {
                    return false; // There's something nearby, avoid placing hazard here
                }
            }
        }
        return true;
    }


    private boolean isValidIndex(Point2D location) {
        int i = (int) location.getX();
        int i1 = (int) location.getY();
        return i >= 0 && i1 >= 0 && i < grid.length && i1 < grid[0].length;
    }


    public int getTile(Point2D location) {
            return grid[(int) location.getY()][(int) location.getX()];
    }

    public void movePlayer(Point2D point2D, Player player) {
        grid[(int) player.getCurrentLocation().getY()][(int) player.getCurrentLocation().getX()] = PREVAL;
        int col = (int) point2D.getX();
        int row = (int) point2D.getY();
        PREVAL = grid[row][col];
        grid[row][col] = PLAYER;
    }

    private ImageView loadImage(String path) {
        Image image = new Image(getClass().getResourceAsStream(path));
        // Create an ImageView and set its size to 50x50
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        return imageView;
    }

    public ImageView getImage(int i, int j) {
        switch (getTile(new Point2D(i,j))){
            case 0:
                return groundTile;
            case 1:
                return wumpustile;
            case 2:
                return pitTile;
            case 3:
                return spidertile;
            case 4:
                return goldTile;
            case 5:
                return webTile;
            case 6:
                return windTile;
            case 7:
                return stinkTtie;
            case 8:
                return glitterTile;
            case 9:
                return guy;
            default:
                return blackTile;
        }
    }

    public ImageView getBlackTile() {
        return blackTile;
    }

    public ImageView getDownArrow() {
        return downArrow;
    }

    public ImageView getEmptyChest() {
        return emptyChest;
    }

    public ImageView getGlitterTile() {
        return glitterTile;
    }

    public ImageView getGoldTile() {
        return goldTile;
    }

    public ImageView getGroundTile() {
        return groundTile;
    }

    public ImageView getGuy() {
        return guy;
    }

    public ImageView getLeftArrow() {
        return leftArrow;
    }

    public ImageView getPitTile() {
        return pitTile;
    }

    public ImageView getPlus() {
        return plus;
    }

    public ImageView getQuestion() {
        return question;
    }

    public ImageView getRightArrow() {
        return rightArrow;
    }

    public ImageView getSpidertile() {
        return spidertile;
    }

    public ImageView getStinkTtie() {
        return stinkTtie;
    }

    public ImageView getTrophy() {
        return trophy;
    }

    public ImageView getUpArrow() {
        return upArrow;
    }

    public ImageView getWebTile() {
        return webTile;
    }

    public ImageView getWindTile() {
        return windTile;
    }

    public ImageView getWumpustile() {
        return wumpustile;
    }

    public int[][] getGrid() {
        return grid;
    }

    private void printGrid(){
        for (int[] i : grid){
            for (int j : i){
                System.out.print(j + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
