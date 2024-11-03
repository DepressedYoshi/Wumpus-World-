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
    private List<Point2D> returnPath = new ArrayList<>(); // Stores the reconstructed path


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
            foundTreasure = true;
            Player.setHasGold(true);
            reconstructPath(currentLocation); // Build return path to start
        }

        closedList.add(currentLocation);
        String direction = decideNextMove();

        if (direction != null && player.move(direction, world)) {
            currentLocation = player.getCurrentLocation();
            backtracking = false;
        } else {
            System.out.println("Stuck at " + currentLocation + "; initiating backtrack.");
            backtracking = true;
        }
    }

    private String decideNextMove() {
        if (foundTreasure && !returnPath.isEmpty()) {
            // Follow the reconstructed path back to the start
            return followReturnPath();
        }
        // Check if the AI detects a sensory tile, triggering backtracking if needed
        if (detectsSensoryTile(currentLocation)) {
            System.out.println("Sensory tile detected at " + currentLocation + ". Initiating backtrack.");
            backtracking = true;
        }

        // Determine next move based on backtracking state
        return backtracking ? backtrack() : selectBestMove(currentLocation);
    }

    private String followReturnPath() {
        if (returnPath.isEmpty()) {
            System.out.println("Return path is empty; AI has reached the start.");
            return null;
        }
        // Move towards the next position in the return path
        Point2D nextPosition = returnPath.remove(returnPath.size()-1);
        if (nextPosition.equals(currentLocation)){
            nextPosition = returnPath.remove(returnPath.size()-1);
        }
        return getDirectionTo(currentLocation, nextPosition);
    }

    private boolean detectsSensoryTile(Point2D position) {
        int tile = world.getBackTile(position);
        int realTile = tile > 11 ? tile - 20 : tile;

        return realTile == World.BREEZ || realTile == World.WEB || realTile == World.STINK;
    }


    private void reconstructPath(Point2D endPosition) {
        returnPath.clear(); // Clear any existing path data
        Node currentNode = nodes.get(endPosition);

        while (currentNode != null) {
            if (!returnPath.isEmpty()) {
                Point2D lastPosition = returnPath.get(0);

                // Check if the node is adjacent to the last position in returnPath
                if (Math.abs(lastPosition.getX() - currentNode.position.getX()) <= 1 &&
                        Math.abs(lastPosition.getY() - currentNode.position.getY()) <= 1) {
                    returnPath.add(0, currentNode.position); // Add to the beginning
                } else {
                    System.err.println("Non-adjacent node found in reconstructPath. Skipping node: " + currentNode.position);
                }
            } else {
                returnPath.add(0, currentNode.position); // Add the first node unconditionally
            }
            currentNode = currentNode.parent;
        }
    }




    private String selectBestMove(Point2D currentPosition) {
        List<String> directions = Arrays.asList("up", "down", "left", "right");
        String bestDirection = null;
        double lowestF = Double.MAX_VALUE;

        for (String direction : directions) {
            Point2D neighborPos = getNeighborPosition(currentPosition, direction);

            if (isValidMove(neighborPos) && !closedList.contains(neighborPos)) {
                double g = nodes.get(currentPosition).g + 1; // Assuming uniform movement cost of 1
                double h = calculateHeuristic(neighborPos);
                double f = g + h;

                // Check if neighbor node exists and if the new g cost is lower than the existing one
                if (!nodes.containsKey(neighborPos) || g < nodes.get(neighborPos).g) {
                    Node neighborNode = new Node(neighborPos, g, h, nodes.get(currentPosition));
                    nodes.put(neighborPos, neighborNode);

                    // Only add to openList if not in closedList
                    if (!closedList.contains(neighborPos)) {
                        openList.add(neighborNode);
                    }
                }

                // Select the direction with the lowest f cost
                if (f < lowestF) {
                    lowestF = f;
                    bestDirection = direction;
                }
            }
        }

        // If no valid direction is found, initiate backtracking
        return bestDirection == null ? backtrack() : bestDirection;

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
        return null;
    }
    // Determines direction from current to a target position
    private String getDirectionTo(Point2D from, Point2D to) {
        if (to.getX() == from.getX() && to.getY() == from.getY() - 1) return "up";
        if (to.getX() == from.getX() && to.getY() == from.getY() + 1) return "down";
        if (to.getX() == from.getX() - 1 && to.getY() == from.getY()) return "left";
        if (to.getX() == from.getX() + 1 && to.getY() == from.getY()) return "right";
        System.out.println("dude, you are trying to get to " + to);
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
                case World.GLITTER -> heuristic -= 15; // Incentive for treasure
                default -> heuristic -= 2; // Encourage exploring unknown tiles
            }

            //I have not idea what chat gpt wrote here - it works
            List<Point2D> goal = findTreasure();
            if (goal != null && !goal.isEmpty()) {
                double closestCandidate = goal.stream()
                        .mapToDouble(candidate -> Math.abs(position.getX() - candidate.getX()) +
                                Math.abs(position.getY() - candidate.getY()))
                        .min()
                        .orElse(Double.MAX_VALUE);
                heuristic += closestCandidate;
            }
        }
        System.out.println("Heuristic for position " + position + " is: " + heuristic);
        return heuristic;
    }

    private void updateDanger(Point2D sensoryPosition) {

        for (Point2D pos : getAdjacentPositions(sensoryPosition)) {
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

    private List<Point2D> findTreasure() {
            List<Point2D> candidates = new ArrayList<>();

            // Iterate over explored tiles, looking for GLITTER indicators
            for (Map.Entry<Point2D, Node> entry : nodes.entrySet()) {
                Point2D glitterPosition = entry.getKey();

                // Check if the current tile has GLITTER
                if (world.getBackTile(glitterPosition) == World.GLITTER) {
                    // Generate potential treasure locations adjacent to the GLITTER tile
                    for (Point2D adjacent : getAdjacentPositions(glitterPosition)) {
                        if (isWithinBounds(adjacent) && !closedList.contains(adjacent)) {
                            candidates.add(adjacent); // Add valid adjacent tiles as treasure candidates
                        }
                    }
                }
            }

            return candidates.isEmpty() ? null : candidates;
    }

    private List<Point2D> getAdjacentPositions(Point2D position) {
        return Arrays.asList(
                new Point2D(position.getX(), position.getY() - 1), // Up
                new Point2D(position.getX(), position.getY() + 1), // Down
                new Point2D(position.getX() - 1, position.getY()), // Left
                new Point2D(position.getX() + 1, position.getY())  // Right
        );
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
            this.parent = null;
        }
        Node(Point2D position, double g, double h, Node parent) {
            this.position = position;
            this.g = g;
            this.h = h;
            this.f = g + h;
            this.parent = parent; // Set the parent node
        }


        double getF() {
            return f;
        }
    }
}
