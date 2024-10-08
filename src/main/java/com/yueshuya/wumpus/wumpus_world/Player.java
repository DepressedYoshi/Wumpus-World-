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
        moveHistory.push(new Point2D(currentLocation.getY(), currentLocation.getX()));

        switch (direction.toLowerCase()) {
            case "up":
                currentLocation = new Point2D(currentLocation.getY() - 1, currentLocation.getX());
                break;
            case "down":
                currentLocation = new Point2D(currentLocation.getY() + 1, currentLocation.getX());
                break;
            case "left":
                currentLocation = new Point2D(currentLocation.getY(), currentLocation.getX() - 1);
                break;
            case "right":
                currentLocation = new Point2D(currentLocation.getY(), currentLocation.getX() + 1);
                break;
            default:
                return false;
        }

        return true;
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
