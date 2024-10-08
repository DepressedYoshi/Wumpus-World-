package com.yueshuya.wumpus.wumpus_world;

import javafx.geometry.Point2D;

import java.util.Random;

public class World {
    public static final int EMPTY = 0;
    public static final int WUMPUS = 1;
    public static final int PIT = 2;
    public static final int SPIDER = 3;
    public static final int TREASURE = 4;

    private int[][] grid;
    private Random random;

    public World(int rows, int cols) {
        grid = new int[rows][cols];
        random = new Random();
    }

    public void populateWorld() {
        placeHazard(WUMPUS, 1);
        placeHazard(PIT, random.nextInt(6) + 5);  // Random number of pits (5-10)
        placeHazard(SPIDER, random.nextInt(3) + 3);  // Random number of spiders (3-5)
        placeTreasure();
    }

    private void placeHazard(int hazard, int count) {
        while (count > 0) {
            int row = random.nextInt(grid.length);
            int col = random.nextInt(grid[0].length);
            if (grid[row][col] == EMPTY) {
                grid[row][col] = hazard;
                count--;
            }
        }
    }

    private void placeTreasure() {
        int row = random.nextInt(grid.length);
        int col = random.nextInt(grid[0].length);
        grid[row][col] = TREASURE;
    }

    public int getTile(Point2D location) {
        return grid[(int) location.getY()][(int) location.getX()];
    }
}
