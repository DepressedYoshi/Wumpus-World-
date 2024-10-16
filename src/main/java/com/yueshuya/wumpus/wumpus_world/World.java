package com.yueshuya.wumpus.wumpus_world;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;
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
    public static final int EMPTY_CHEST = 10;
    public static final int SIZE=50;
    private static int PREVAL = 0;

    private static boolean gameover = false;
    private final int[][] neighbor = {
            {-1,0}, {0,1}, {1,0},{0,-1}, {-1,-1,},{-1,1},{1,1},{1,-1}
    };
    private int[][] grid;
    private Random random;
    private final Player player;

    private final ImageView blackTile = loadImage("/lib/blackTile.png");
    private final ImageView downArrow = loadImage("/lib/downArrow.png");
    private final ImageView emptyChest = loadImage("/lib/emptyChest.png");
    private final ImageView glitterTile = loadImage("/lib/glitterTile.png");
    private final ImageView goldTile = loadImage("/lib/goldTile.png");
    private final ImageView groundTile = loadImage("/lib/groundTile.png");
    private final ImageView guy = loadImage("/lib/guy.png");
    private final ImageView leftArrow = loadImage("/lib/leftArrow.png");
    private final ImageView pitTile = loadImage("/lib/pitTile.png");
    private final ImageView plus = loadImage("/lib/plus.png");
    private final ImageView question = loadImage("/lib/question.png");
    private final ImageView rightArrow = loadImage("/lib/rightArrow.png");
    private final ImageView spidertile = loadImage("/lib/spiderTile.png");
    private final ImageView stinkTtie = loadImage("/lib/stinkTile.png");
    private final ImageView trophy = loadImage("/lib/trophy.png");
    private final ImageView upArrow = loadImage("/lib/upArrow.png");
    private final ImageView webTile = loadImage("/lib/webTile.png");
    private final ImageView windTile = loadImage("/lib/windTile.png");
    private final ImageView wumpustile = loadImage("/lib/wumpusTile.png");

    public static boolean isGameover() {
        return gameover;
    }

    public static void setGameover(boolean gameover) {
        World.gameover = gameover;
    }

    public World(int rows, int cols, Player player) {
        grid = new int[rows][cols];
        random = new Random();
        this.player = player;
        placePlayer();
        populateWorld();
        genFogOfWar();
    }

    private void placePlayer() {
        int row = (int) player.getCurrentLocation().getY();
        int col = (int) player.getCurrentLocation().getX();
        grid[row][col] = PLAYER;
    }

    private void populateWorld() {
        placeTreasure();
        genHazrd(WUMPUS,1);
        genHazrd(PIT,2);
        genHazrd(SPIDER,2);
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
        if (grid[row][col] != EMPTY) {return false;}

        for(int[] n : neighbor) {
            if (isValidIndex(new Point2D(row + n[1], col + n[0]))) {
                if (grid[row + n[1]][col + n[0]] != EMPTY) {
                    return false;
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
        int col = (int) point2D.getX();
        int row = (int) point2D.getY();
        //check if player hit the sput
        if (getRealPre() == TREASURE){
            grid[(int) player.getCurrentLocation().getY()][(int) player.getCurrentLocation().getX()] = EMPTY_CHEST;
        }
        else {
            grid[(int) player.getCurrentLocation().getY()][(int) player.getCurrentLocation().getX()] = getRealPre();
        }
        PREVAL = grid[row][col];
        grid[row][col] = PLAYER;
    }

    public void genFogOfWar(){
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] != PLAYER){
                    genFog(i, j);
                }
            }
        }
    }
    private void genFog(int row, int col){
        int val = grid[row][col];
        if (val <= 10){
            grid[row][col] += 20;
        }
    }
    public void clearAllFog(){
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                clearFog(i, j);
            }
        }
    }

    private void clearFog(int row, int col) {
        int val = grid[row][col];
        if (val > 15){
            grid[row][col] -= 20;
        }
    }

    private ImageView loadImage(String path) {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        return imageView;
    }

    public ImageView getImage(int i) {
        return switch (i) {
            case EMPTY -> groundTile;
            case WUMPUS -> wumpustile;
            case PIT -> pitTile;
            case SPIDER -> spidertile;
            case TREASURE -> goldTile;
            case WEB -> webTile;
            case BREEZ -> windTile;
            case STINK -> stinkTtie;
            case GLITTER -> glitterTile;
            case PLAYER -> guy;
            case EMPTY_CHEST -> emptyChest;
            default -> blackTile;
        };
    }

    public void reset() {
        PREVAL = 0;
        placePlayer();
        genFogOfWar();
    }

    public void clear() {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j]==PLAYER){
                    grid[i][j] = getRealPre();
                }
                if (grid[i][j] == EMPTY_CHEST){
                    grid[i][j] = TREASURE;
                }
            }
        }
    }

    public int getRealPre() {
        if (PREVAL > 10){
            return PREVAL - 20;
        }
        return PREVAL;
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

    public ImageView getRealBackground() {
        return getImage(getRealPre());

    }
}
