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
            System.out.println("the AI completed the mission");
            return;
        }
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
                goBack();
            }
            closedList.add(currentLocation);
            String direction ;
            if (!backtracking) {
                direction = selectBestMove(currentLocation);
            } else {
                direction = backtrack();
            }

            if (direction != null) {
                System.out.println("AI selected direction: " + direction);
                boolean moved = player.move(direction, world);
                if (moved) {
                    currentLocation = player.getCurrentLocation();
                    System.out.println("Moved to new location: " + currentLocation);
                    backtracking = false;
                } else {
                    System.out.println("Move failed: " + direction);
                }
            } else {
                System.out.println("No valid direction found; AI is stuck.");
                backtracking = true;
            }
        }
    }

    private void goBack() {
        openList.clear();
        closedList.clear();
        nodes.clear();
        foundTreasure = true;
        Player.setHasGold(true);
        System.out.println("Treasure found! Returning to start.");
    }

    private String selectBestMove(Point2D currentPosition) {
        List<String> directions = Arrays.asList("up", "down", "left", "right");
        String bestDirection = null;
        double lowestHeuristic = Double.MAX_VALUE;

        for (String direction : directions) {
            Point2D neighborPos = getNeighborPosition(currentPosition, direction);

            if (isValidMove(neighborPos) && !closedList.contains(neighborPos)) {
                double heuristic = calculateLookaheadHeuristic(neighborPos, 3);
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
            backtracking = true;
        }

        return bestDirection;
    }
    private double calculateLookaheadHeuristic(Point2D position, int depth) {
        if (depth == 0 || closedList.contains(position)) {
            return calculateHeuristic(position);
        }

        closedList.add(position);
        double lowestHeuristic = Double.MAX_VALUE;

        for (String direction : Arrays.asList("up", "down", "left", "right")) {
            Point2D nextPos = getNeighborPosition(position, direction);
            if (isValidMove(nextPos)) {
                double heuristic = calculateLookaheadHeuristic(nextPos, depth - 1);
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
                System.out.println("Backtracking to previous location: " + previousPosition);
                return directionToPrevious;
            }
        }

        System.out.println("Backtracking exhausted; no moves left.");
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
        int realTile = tile > 20 ? tile - 20 : tile; // Adjust fogged values for heuristic

        if (foundTreasure) {
            // Calculate heuristic to reach the start if treasure is found
            heuristic += Math.abs(position.getX() - player.getStartLocation().getX()) +
                    Math.abs(position.getY() - player.getStartLocation().getY());
        } else {
            // Calculate heuristic based on nearby hazards and treasure proximity
            switch (realTile) {
                case World.BREEZ -> heuristic += 5;
                case World.WEB -> heuristic += 5;
                case World.STINK -> heuristic += 10;
                case World.GLITTER -> heuristic -= 50; // Strong incentive for treasure
            }
            Point2D treasureLocation = findTreasure();
            if (treasureLocation != null) {
                heuristic += Math.abs(position.getX() - treasureLocation.getX()) +
                        Math.abs(position.getY() - treasureLocation.getY());
            }
        }

        if (tile > 20) heuristic += 2;

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

    public boolean isFoundTreasure() {
        return foundTreasure;
    }

    public void setFoundTreasure(boolean foundTreasure) {
        this.foundTreasure = foundTreasure;
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
