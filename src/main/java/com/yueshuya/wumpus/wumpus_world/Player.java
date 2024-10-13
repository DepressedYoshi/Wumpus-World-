package com.yueshuya.wumpus.wumpus_world;

import javafx.geometry.Point2D;

import java.util.Stack;

public class Player {
    private Point2D currentLocation;
    private Stack<Point2D> moveHistory;

    public Player(Point2D startLocation) {
        currentLocation = startLocation;
        moveHistory = new Stack<>();
    }

    public boolean move(String direction, World world) {
        moveHistory.push(currentLocation);  // Correct coordinate order

        Point2D newLocation;

        switch (direction.toLowerCase()) {
            case "up":
                newLocation = new Point2D(currentLocation.getX(), currentLocation.getY() - 1);  // Moving up reduces the Y-coordinate
                break;
            case "down":
                newLocation = new Point2D(currentLocation.getX(), currentLocation.getY() + 1);  // Moving down increases the Y-coordinate
                break;
            case "left":
                newLocation = new Point2D(currentLocation.getX() - 1, currentLocation.getY());  // Moving left reduces the X-coordinate
                break;
            case "right":
                newLocation = new Point2D(currentLocation.getX() + 1, currentLocation.getY());  // Moving right increases the X-coordinate
                break;
            default:
                return false;
        }

        // Check if the new location is within the boundaries of the world
        if (isWithinBounds(newLocation, world)) {
            world.movePlayer(newLocation, this);
            currentLocation = newLocation;
            return true;
        } else {
            // If out of bounds, don't move the player
            System.out.println("player think its out of bounds - did nto moure");
            moveHistory.pop();  // Undo the move history push
            return false;
        }
    }

    private boolean isWithinBounds(Point2D location, World world) {
        int rows = world.getGrid().length;        // Y-dimension
        int cols = world.getGrid()[0].length;     // X-dimension
        double x = location.getX();
        double y = location.getY();

        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    public void backtrack() {
        if (!moveHistory.isEmpty()) {
            currentLocation = moveHistory.pop();
        }
    }

    public Point2D getCurrentLocation() {
        return currentLocation;
    }
}
