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
    private boolean backtracking = false;

    public AIPlayer(Player player, World world) {
        this.player = player;
        this.world = world;
        this.openList = new PriorityQueue<>(Comparator.comparingDouble(Node::getF));
        this.closedList = new HashSet<>();
        this.nodes = new HashMap<>();

        this.currentLocation = player.getStartLocation();
    }

    public void runAI() {
        if (foundTreasure && player.getCurrentLocation().equals(player.getStartLocation())){
            return;
        }
        if (openList.isEmpty()) {
            Node startNode = new Node(currentLocation, 0, calculateHeuristic(currentLocation));
            openList.add(startNode);
            nodes.put(currentLocation, startNode);
        }

        if (world.getRealPre() == World.TREASURE) {
            goBack();
        }
        closedList.add(currentLocation);
        String direction = decideNextMove();

        if (direction != null) {
            boolean moved = player.move(direction, world);
            if (moved) {
                currentLocation = player.getCurrentLocation();
                backtracking = false;
            } else {
                System.out.println("Stucked - please debugg");
            }
        } else {
            backtracking = true;
        }
    }

    private String decideNextMove() {
        // Check if the AI detects a sensory tile, triggering backtracking if needed
        if (detectsSensoryTile(currentLocation)) {
            System.out.println("Sensory tile detected at " + currentLocation + ". Initiating backtrack.");
            backtracking = true;
        }

        // Determine next move based on backtracking state
        return backtracking ? backtrack() : selectBestMove(currentLocation);
    }
    private boolean detectsSensoryTile(Point2D position) {
        int tile = world.getBackTile(position);
        int realTile = tile > 11 ? tile - 20 : tile;

        return realTile == World.BREEZ || realTile == World.WEB || realTile == World.STINK;
    }


    private void goBack() {
        openList.clear();
        closedList.clear();
        nodes.clear();
        foundTreasure = true;
        Player.setHasGold(true);
    }

    private String selectBestMove(Point2D currentPosition) {
        List<String> directions = Arrays.asList("up", "down", "left", "right");
        String bestDirection = null;
        double lowestHeuristic = Double.MAX_VALUE;

        for (String direction : directions) {
            Point2D neighborPos = getNeighborPosition(currentPosition, direction);

            if (isValidMove(neighborPos) && !closedList.contains(neighborPos)) {
                double heuristic = lookAhead(neighborPos, 10);

                if (heuristic < lowestHeuristic) {
                    lowestHeuristic = heuristic;
                    bestDirection = direction;
                }
            }
        }

        if (bestDirection == null) {
            backtracking = true;
        }

        return bestDirection;
    }
    private double lookAhead(Point2D position, int depth) {
        if (depth == 0 || closedList.contains(position)) {
            return calculateHeuristic(position);
        }

        closedList.add(position);
        double lowestHeuristic = Double.MAX_VALUE;

        for (String direction : Arrays.asList("up", "down", "left", "right")) {
            Point2D nextPos = getNeighborPosition(position, direction);
            if (isValidMove(nextPos)) {
                double heuristic = lookAhead(nextPos, depth - 1);
                lowestHeuristic = Math.min(lowestHeuristic, heuristic);
            }
        }

        closedList.remove(position); // Restore closed list for accurate tracking
        return lowestHeuristic;
    }

    private String backtrack(){
        while (!player.getMoveHistory().isEmpty()) {
            // Retrieve the last position in history
            Point2D previousPosition = player.getMoveHistory().pop();

            // Find direction to move back towards this previous position
            String directionToPrevious = getDirectionTo(currentLocation, previousPosition);

            // Attempt to move back if valid
            if (directionToPrevious != null && isValidMove(previousPosition)) {
                return directionToPrevious;
            }
        }
        return null; // If move history is empty, return null

    }
    // Determines direction from current to a target position
    private String getDirectionTo(Point2D from, Point2D to) {
        if (to.getX() == from.getX() && to.getY() == from.getY() - 1) return "up";
        if (to.getX() == from.getX() && to.getY() == from.getY() + 1) return "down";
        if (to.getX() == from.getX() - 1 && to.getY() == from.getY()) return "left";
        if (to.getX() == from.getX() + 1 && to.getY() == from.getY()) return "right";
        return null;
    }

    private double calculateHeuristic(Point2D position) {
        double heuristic = 0;
        int tile = world.getBackTile(position);
        int realTile = tile > 11 ? tile - 20 : tile; // Adjust fogged values for heuristic

        if (foundTreasure) {
            // Heuristic to prioritize reaching the start if treasure is found
            heuristic += Math.abs(position.getX() - player.getStartLocation().getX()) +
                    Math.abs(position.getY() - player.getStartLocation().getY());
        } else {
            // Adjust heuristic based on nearby hazards and treasure proximity
            switch (realTile) {
                case World.BREEZ, World.WEB, World.STINK -> {
                    heuristic += 10; // Moderate penalty for sensory indicators
                    updateDanger(position); // Mark surrounding tiles as potentially dangerous
                }
                case World.SPIDER, World.WUMPUS, World.PIT -> heuristic += 50; // Strong penalty for direct hazards
                case World.GLITTER -> heuristic -= 50; // Incentive for treasure
                default -> heuristic -= 2; // Encourage exploring unknown tiles
            }

            // Encourage moves closer to treasure location if known
            Point2D treasureLocation = findTreasure();
            if (treasureLocation != null) {
                heuristic += Math.abs(position.getX() - treasureLocation.getX()) +
                        Math.abs(position.getY() - treasureLocation.getY());
            }
        }


        System.out.println("Heuristic for position " + position + " is: " + heuristic);
        return heuristic;
    }

    private void updateDanger(Point2D sensoryPosition) {
        List<Point2D> adjacentPositions = Arrays.asList(
                new Point2D(sensoryPosition.getX(), sensoryPosition.getY() - 1), // Up
                new Point2D(sensoryPosition.getX(), sensoryPosition.getY() + 1), // Down
                new Point2D(sensoryPosition.getX() - 1, sensoryPosition.getY()), // Left
                new Point2D(sensoryPosition.getX() + 1, sensoryPosition.getY())  // Right
        );

        for (Point2D pos : adjacentPositions) {
            if (isWithinBounds(pos) && !closedList.contains(pos)) {
                double hazardHeuristic = 20; // Increase heuristic for adjacent danger perception
                Node node = nodes.getOrDefault(pos, new Node(pos));
                node.h += hazardHeuristic;
                node.f = node.g + node.h;
                nodes.put(pos, node); // Update nodes map with potential hazard heuristic
            }
        }
    }

    // Utility method to check if a position is within grid bounds
    private boolean isWithinBounds(Point2D position) {
        return position.getX() >= 0 && position.getX() < world.getGrid().length &&
                position.getY() >= 0 && position.getY() < world.getGrid()[0].length;
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
        return position.getX() >= 0 && position.getX() < 10 &&
                position.getY() >= 0 && position.getY() < 10 &&
                world.getBackTile(position) != World.WUMPUS &&
                world.getBackTile(position) != World.PIT &&
                world.getBackTile(position) != World.SPIDER;
    }

    private Point2D findTreasure() {
        for (Map.Entry<Point2D, Node> entry : nodes.entrySet()) {
            if (world.getBackTile(entry.getKey()) == World.GLITTER) {
                return entry.getKey();
            }
        }
        return null;
    }


    public void reset() {
        player.reset();
        openList.clear();
        closedList.clear();
        nodes.clear();
        foundTreasure = false;
        backtracking = false;
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
