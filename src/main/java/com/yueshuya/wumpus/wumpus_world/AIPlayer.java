package com.yueshuya.wumpus.wumpus_world;

import javafx.geometry.Point2D;
import java.util.*;

public class AIPlayer {
    private final Player player;
    private final World world;
    private final PriorityQueue<Node> openList;
    private final Set<Point2D> closedList;
    private final Map<Point2D, Node> nodes;

    private Point2D currentLocation;
    private boolean foundTreasure = false;

    public AIPlayer(Player player, World world) {
        this.player = player;
        this.world = world;
        this.openList = new PriorityQueue<>(Comparator.comparingDouble(Node::getF));
        this.closedList = new HashSet<>();
        this.nodes = new HashMap<>();

        this.currentLocation = player.getStartLocation();
    }

    public void runAI() {
        if (openList.isEmpty()) {
            Node startNode = new Node(currentLocation, 0, calculateHeuristic(currentLocation));
            openList.add(startNode);
            nodes.put(currentLocation, startNode);
        }

        // Run one step of the AI's movement
        if (!openList.isEmpty()) {
            Node current = openList.poll();
            System.out.println("Current AI Location: " + currentLocation);

            if (world.getRealPre() == World.TREASURE) {
                foundTreasure = true;
                Player.setHasGold(true);
                System.out.println("Treasure found! Returning to start.");
                return;
            }

            closedList.add(currentLocation);

            String direction = selectBestMove(currentLocation);
            if (direction != null) {
                System.out.println("AI selected direction: " + direction);
                boolean moved = player.move(direction, world);
                if (moved) {
                    currentLocation = player.getCurrentLocation();
                    System.out.println("Moved to new location: " + currentLocation);
                } else {
                    System.out.println("Move failed: " + direction);
                }
            } else {
                System.out.println("No valid direction found; AI is stuck.");
            }
        }
    }

    private String selectBestMove(Point2D currentPosition) {
        List<String> directions = Arrays.asList("up", "down", "left", "right");
        String bestDirection = null;
        double lowestHeuristic = Double.MAX_VALUE;

        for (String direction : directions) {
            Point2D neighborPos = getNeighborPosition(currentPosition, direction);

            if (isValidMove(neighborPos) && !closedList.contains(neighborPos)) {
                double heuristic = calculateHeuristic(neighborPos);
                System.out.println("Evaluating direction " + direction + " with heuristic: " + heuristic);

                if (heuristic < lowestHeuristic) {
                    lowestHeuristic = heuristic;
                    bestDirection = direction;
                }
            } else {
                System.out.println("Direction " + direction + " is invalid or already visited.");
            }
        }

        if (bestDirection == null) {
            System.out.println("Fallback needed: no optimal move found.");
            for (String direction : directions) {
                Point2D fallbackPos = getNeighborPosition(currentPosition, direction);
                if (isValidMove(fallbackPos)) {
                    bestDirection = direction;
                    System.out.println("Using fallback direction: " + bestDirection);
                    break;
                }
            }
        }

        return bestDirection;
    }

    private double calculateHeuristic(Point2D position) {
        double heuristic = 0;
        int tile = world.getBackTile(position);

        switch (tile) {
            case World.BREEZ -> heuristic += 5;
            case World.WEB -> heuristic += 5;
            case World.STINK -> heuristic += 10;
            case World.GLITTER -> heuristic -= 50;
        }

        Point2D treasureLocation = findTreasure();
        if (treasureLocation != null) {
            heuristic += Math.abs(position.getX() - treasureLocation.getX()) +
                    Math.abs(position.getY() - treasureLocation.getY());
        }

        System.out.println("Heuristic for position " + position + " is: " + heuristic);
        return heuristic;
    }

    private Point2D getNeighborPosition(Point2D current, String direction) {
        return switch (direction.toLowerCase()) {
            case "up" -> new Point2D(current.getX(), current.getY() - 1);
            case "down" -> new Point2D(current.getX(), current.getY() + 1);
            case "left" -> new Point2D(current.getX() - 1, current.getY());
            case "right" -> new Point2D(current.getX() + 1, current.getY());
            default -> current;
        };
    }

    private boolean isValidMove(Point2D position) {
        boolean valid = position.getX() >= 0 && position.getX() < 10 &&
                position.getY() >= 0 && position.getY() < 10 &&
                world.getBackTile(position) != World.WUMPUS &&
                world.getBackTile(position) != World.PIT &&
                world.getBackTile(position) != World.SPIDER;
        if (!valid) {
            System.out.println("Position " + position + " is not a valid move.");
        }
        return valid;
    }

    private Point2D findTreasure() {
        for (Map.Entry<Point2D, Node> entry : nodes.entrySet()) {
            if (world.getBackTile(entry.getKey()) == World.GLITTER) {
                System.out.println("Treasure detected at position: " + entry.getKey());
                return entry.getKey();
            }
        }
        return null;
    }

    private static class Node {
        Point2D position;
        double g;
        double h;
        double f;
        Node parent;

        Node(Point2D position) {
            this.position = position;
            this.g = Double.POSITIVE_INFINITY;
            this.h = 0;
            this.f = g + h;
        }

        Node(Point2D position, double g, double h) {
            this.position = position;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        double getF() {
            return f;
        }
    }
}
