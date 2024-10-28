package com.yueshuya.wumpus.wumpus_world;
import javafx.geometry.Point2D;
import java.util.*;

public class AIPlayer {
    private static Player player;
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
        // Initial setup if AI starts from beginning
        if (openList.isEmpty()) {
            Node startNode = new Node(currentLocation, 0, calculateHeuristic(currentLocation));
            openList.add(startNode);
            nodes.put(currentLocation, startNode);
        }

        // Run one step of the AI's movement
        if (!openList.isEmpty()) {
            Node current = openList.poll();

            // If treasure is found, set flag
            if (world.getRealPre() == World.TREASURE) {
                foundTreasure = true;
                Player.setHasGold(true);
                return;
            }

            // Mark current node as explored
            closedList.add(currentLocation);

            // Explore neighbors and decide the move
            String direction = selectBestMove(currentLocation);
            if (direction != null) {
                boolean moved = player.move(direction, world);  // Execute move
                if (moved) {
                    // Update current location and player state after moving
                    currentLocation = player.getCurrentLocation();
                }
            }
        }
    }

    // Decides the next direction to move based on A* algorithm evaluation
    private String selectBestMove(Point2D currentPosition) {
        List<String> directions = Arrays.asList("up", "down", "left", "right");
        String bestDirection = null;
        double lowestHeuristic = Double.MAX_VALUE;

        for (String direction : directions) {
            Point2D neighborPos = getNeighborPosition(currentPosition, direction);

            // Ensure the position is valid and not already visited
            if (isValidMove(neighborPos) && !closedList.contains(neighborPos)) {
                System.out.println("we are calculating: " + neighborPos);
                double heuristic = calculateHeuristic(neighborPos);
                if (heuristic < lowestHeuristic) {
                    lowestHeuristic = heuristic;
                    bestDirection = direction;
                }
            }
        }

        // Fallback if no optimal move is found: select the first valid direction
        if (bestDirection == null) {
            for (String direction : directions) {
                Point2D fallbackPos = getNeighborPosition(currentPosition, direction);
                if (isValidMove(fallbackPos)) {
                    bestDirection = direction;
                    break;
                }
            }
        }

        System.out.println("The best direction is " + bestDirection);
        return bestDirection; // Returns null if no valid direction found
    }

    private double calculateHeuristic(Point2D position) {
        double heuristic = 0;
        int tile = world.getTile(position);
        if (tile == World.PLAYER){
            tile = world.getRealPre();
        }
        switch (tile) {
            case World.BREEZ -> heuristic += 5;
            case World.WEB -> heuristic += 5;
            case World.STINK -> heuristic += 10;
            case World.GLITTER -> heuristic -= 50;
        }

        // Manhattan distance to the treasure location (if known)
        Point2D treasureLocation = findTreasure();
        if (treasureLocation != null) {
            heuristic += Math.abs(position.getX() - treasureLocation.getX()) +
                    Math.abs(position.getY() - treasureLocation.getY());
        }

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
        return player.getMoveHistory().contains(position) &&
                position.getX() >= 0 && position.getX() < 10 &&
                position.getY() >= 0 && position.getY() < 10 &&
                world.getTile(position) != World.WUMPUS &&
                world.getTile(position) != World.PIT &&
                world.getTile(position) != World.SPIDER;
    }

    private Point2D findTreasure() {
        for (Map.Entry<Point2D, Node> entry : nodes.entrySet()) {
            if (world.getTile(entry.getKey()) == World.GLITTER) {
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
