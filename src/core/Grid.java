package core;

import UI.Motion;
import core.player.Player;
import objects.GameObject;
import objects.units.Unit;

import java.util.*;

import static core.GameConstants.*;

public class Grid extends HashMap<Location, Cell> {

    private int[][] pathCosts, totalCosts;
    private Cell src;

    public Grid(int size) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Cell cell = new Cell(i, j, 0, INITIAL_CELL_UNIT_SPACE, INITIAL_CELL_BUILDING_SPACE);
                put(cell.getLocation(), cell);
            }
        }

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Cell cell = get(new Location(x, y, 0));
                if(x > 0)
                    cell.link(get(new Location(x - 1, y, 0)));
                if(y > 0)
                    cell.link(get(new Location(x, y - 1, 0)));
                if(x < size - 1)
                    cell.link(get(new Location(x + 1, y, 0)));
                if(y < size - 1)
                    cell.link(get(new Location(x, y + 1, 0)));
            }
        }
    }

    public static int objectDistance(GameObject<?> obj1, GameObject<?> obj2) {
        return distance(obj1.getCell(), obj2.getCell());
    }

    public static int distance(Cell cell1, Cell cell2) {
        Location loc1 = cell1.getLocation();
        Location loc2 = cell2.getLocation();
        return loc1.distanceTo(loc2);
    }

    public Cell get(Object key) {
        if(containsKey(key))
            return super.get(key);
        else
            throw new IllegalArgumentException("Key not found: " + key);
    }

    public void cycle(int cycle) {
        for(Cell cell : values())
            cell.cycle(cycle);
    }

    /**
     * Calculates the shortest path from the given {@code Cell} to every other {@code Cell} within maxDist (in L1-distance)
     * using the L1-informed A*-algorithm.
     * @param src starting cell
     * @param currentPlayer current player (used for determining undiscovered cells, which act as obstacles)
     * @param maxDist maximum distance
     */
    public void populateDistanceMatrix(Cell src, Player currentPlayer, int maxDist) {
        this.src = src;
        pathCosts = new int[2 * maxDist + 1][2 * maxDist + 1];

//        PriorityQueue<Cell> toExplore = new PriorityQueue<>(Comparator.comparing(
//                cell -> pathCosts[maxDist + (cell.getLocation().x() - src.getX())][maxDist + (cell.getY() - src.getY())] + distance(cell, src)));
        PriorityQueue<Cell> toExplore = new PriorityQueue<>(Comparator.comparing(
                cell -> pathCosts[maxDist + (cell.getX() - src.getX())][maxDist + (cell.getY() - src.getY())]));
        ArrayList<Cell> done = new ArrayList<>();

        for (int x = 0; x < 2 * maxDist + 1; x++)
            for (int y = 0; y < 2 * maxDist + 1; y++)
                pathCosts[x][y] = Integer.MAX_VALUE;
        pathCosts[maxDist][maxDist] = 0;

        done.add(src);
        toExplore.add(src);

        while (!toExplore.isEmpty()) {
            Cell current = toExplore.poll();
            done.add(current);
            int currentCost = pathCosts[maxDist + (current.getX() - src.getX())][maxDist + (current.getY() - src.getY())];

            if (!containsKey(current.getLocation()))
                continue;

            if(currentPlayer.hasSpotted(current))
                for (int x = (src.getX() - current.getX() == maxDist ? 0 : -1); x < (current.getX() - src.getX() == maxDist ? 1 : 2); x++)
                    for (int y = (src.getY() - current.getY() == maxDist ? 0 : -1); y < (current.getY() - src.getY() == maxDist ? 1 : 2); y++)
                        if (Math.abs(x) + Math.abs(y) == 1 && currentPlayer.hasSpotted(current.fetch(x, y, 0))) // Only check direct neighbours that have been spotted
                                pathCosts[maxDist + (current.getX() - src.getX()) + x][maxDist + (current.getY() - src.getY()) + y] = Math.min(pathCosts[maxDist + (current.getX() - src.getX()) + x][maxDist + (current.getY() - src.getY()) + y],
                                    currentCost == Integer.MAX_VALUE ? currentCost : currentCost + current.fetch(x, y, 0).getTravelCost(Direction.NORTH));

            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    if (x * y == 0) {
                        Location candidate = current.getLocation().add(x, y, 0);
                        Cell next = get(candidate);
                        if (src.getLocation().distanceTo(candidate) <= maxDist
                                && !done.contains(next)
                                && !toExplore.contains(next)) {
                            toExplore.add(next);
                        }
                    }
                }
            }
        }

//        printDistances(pathCosts);
    }

    public int getPathDistance(Cell target) {
        Location diff = target.getLocation().add(src.getLocation().negative());
        int maxDist = (pathCosts.length - 1) / 2;
        if(Math.abs(diff.x()) > pathCosts.length || Math.abs(diff.y()) > pathCosts.length || Math.abs(diff.z()) > pathCosts.length)
            return Integer.MAX_VALUE;

        return pathCosts[diff.x() + maxDist][diff.y() + maxDist];
    }

    public ArrayList<Location> getShortestAdmissiblePath(Cell target) throws IllegalArgumentException, IllegalStateException {
        if(target == null)
            throw new IllegalArgumentException("Target should not be null.");

        // TODO Rephrase through getType()? Does lose type safety and pattern matching
//        if(!(obj instanceof Unit<?> unit))
//            throw new IllegalArgumentException("GameObject should be of type Unit.");

        if(target.equals(src))
            return null;
//            return new Pair<>(new Motion(unit, new ArrayList<>(), 0), target.getLocation());

        int maxDist = (pathCosts.length - 1) / 2;
        if(src.distanceTo(target) > maxDist)
            return null;

        if (src.getZ() == target.getZ()) {

            int deltaX = target.getX() - src.getX();
            int deltaY = target.getY() - src.getY();
            int cost = pathCosts[maxDist + deltaX][maxDist + deltaY];

            if (cost > maxDist)
                return null;

            ArrayList<Location> path = new ArrayList<>();
            Location current = target.getLocation();

            while (!current.equals(src.getLocation())) {
                int min = pathCosts[maxDist + (current.x() - src.getX())][maxDist + (current.y() - src.getY())];
                Location temp = null;
                for (int x = (src.getX() - target.getX() == maxDist ? 0 : -1); x < (target.getX() - src.getX() == maxDist ? 1 : 2); x++) {
                    for (int y = (src.getY() - target.getY() == maxDist ? 0 : -1); y < (target.getY() - src.getY() == maxDist ? 1 : 2); y++) {
                        if (Math.abs(x) + Math.abs(y) == 1 &&
                            pathCosts[maxDist + (current.x() - src.getX()) + x][maxDist + (current.y() - src.getY()) + y] < min)
                        {
                            min = pathCosts[maxDist + (current.x() - src.getX()) + x][maxDist + (current.y() - src.getY()) + y];
                            temp = new Location(x, y, 0);
                        }
                    }
                }

                if(temp == null)
                    throw new IllegalStateException("No valid path could be found.");

                current = current.add(temp);
                path.addFirst(temp.negative());
            }
            return path;
        } else
            return null;
    }

    /**
     * For testing purposes!!
     * @param dist
     */
    public static void printDistances(int[][] dist) {
        int width = dist.length;
        int height = dist[0].length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (dist[x][y] == Integer.MAX_VALUE) {
                    System.out.print("  X ");
                } else {
                    System.out.printf("%3d ", dist[x][y]);
                }
            }
            System.out.println();
        }
    }
}
