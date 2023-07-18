package core;

import general.Location;
import resources.Resource;
import resources.ResourceContainer;
import items.GameObject;

import java.util.HashSet;

import static core.GameConstants.*;
import static resources.Resource.*;

public class Cell {

    private int unitSpace, unitOccupied, buildingSpace, buildingOccupied, travelCost, heatLevel;
    private final ResourceContainer resources;
    private int x, y, z;

    private Cell east, west, north, south, up, down;
    private boolean linking;
    private final HashSet<GameObject> content;

    public Cell(int x, int y, int z, int s, int b) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.linking = false;
        this.content = new HashSet<>();
        unitSpace = s;
        buildingSpace = b;
        travelCost = INITIAL_TRAVEL_COST;
        resources = generateResources();
        seasonalCycle(0);
    }

    public void cycle(int cycle) {

        if(cycle % SEASON_LENGTH == 0)
            seasonalCycle((cycle % (4 * SEASON_LENGTH)) / SEASON_LENGTH);

        if(heatLevel >= HOT_LEVEL)
            resources.put(WATER, Math.max(resources.get(WATER) - 2, 0));
    }

    public int getUnitSpace() {
        return unitSpace;
    }

    public int getUnitOccupied() {
        return unitOccupied;
    }

    public int getUnitAvailable() {
        return unitSpace - unitOccupied;
    }

    public void changeUnitSpace(int amount) {
        unitSpace += amount;
    }

    public void changeUnitOccupied(int amount) {
        unitOccupied += amount;
    }

    public int getBuildingSpace() {
        return buildingSpace;
    }

    public int getBuildingOccupied() {
        return buildingOccupied;
    }

    public int getBuildingAvailable() {
        return buildingSpace - buildingOccupied;
    }

    public void changeBuildingSpace(int amount) {
        buildingSpace += amount;
    }

    public void changeBuildingOccupied(int amount) {
        buildingOccupied += amount;
    }

    public int getTravelCost() {
        int cost = travelCost;
        if(resources.get(WATER) >= WATER_THRESHOLD)
            cost += 2;
        cost += Math.max(0, heatLevel - HOT_LEVEL);
        return cost;
    }

    public void changeTravelCost(int amount) {
        travelCost += amount;
    }

    public int getResource(Resource type) {
        return resources.get(type);
    }

    public int changeResource(Resource type, int amount) {

        if(amount < 0)
            amount = -Math.min(resources.get(type), -amount);

        if(type == WATER)
            if(heatLevel >= 50 || heatLevel <= COLD_LEVEL)
                amount = 0;

        resources.put(type, resources.get(type) + amount);
        return amount;
    }

    public void changeResources(ResourceContainer res) {
        for(Resource resource : res.keySet())
            changeResource(resource, res.get(resource));
    }

    public int getHeatLevel() { return heatLevel; }

    public void changeHeatLevel(int amount) { heatLevel += amount; }

    public boolean isRiver() { return resources.get(WATER) >= WATER_THRESHOLD; }

    public boolean isForest() { return resources.get(WOOD) >= WOOD_THRESHOLD; }

    public boolean isField() { return resources.get(FOOD) >= FOOD_THRESHOLD; }

    public void addContent(GameObject obj) { content.add(obj); }

    public void removeContent(GameObject obj) { content.remove(obj); }

    public HashSet<GameObject> getContent() { return content; }

    /**
     * Changes parameters of the cell when the season switches
     * @param season 0: Winter, 1: Spring, 2: Summer, 3: Fall
     */
    private void seasonalCycle(int season) {
        heatLevel = INITIAL_HEAT_LEVEL;
        if(season == 0)
            heatLevel -= rand.nextInt(4) + 1;
        else if(season == 1) {
            heatLevel += rand.nextInt(3) - 1;
            if (rand.nextInt(5) >= 3)
                resources.put(WATER, resources.get(WATER) + rand.nextInt(30));
        }
        else if(season == 2)
            heatLevel += rand.nextInt(5) + 1;
        else if(season == 3) {
            heatLevel += rand.nextInt(3) - 1;
            if(rand.nextInt(5) >= 2)
                resources.put(WATER, resources.get(WATER) + rand.nextInt(50));
        }
    }

    /**
     * Generates a random amount of resources to initialize a cell
     * @return HashMap with random amount of resources
     */
    private static ResourceContainer generateResources() {
        ResourceContainer resources = new ResourceContainer();
        resources.put(FOOD, rand.nextInt(200));
        resources.put(WOOD, rand.nextInt(250));
        resources.put(STONE, rand.nextInt(100));
        resources.put(IRON, rand.nextInt(50));
        resources.put(COAL, rand.nextInt(50));
        resources.put(TIME, 0);

        resources.put(WATER, rand.nextInt(200));
        if(resources.get(WATER) > GameConstants.WATER_THRESHOLD)
            resources.put(WATER, 300 + rand.nextInt(100));

        return resources;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void link(Cell cell) {
        int dx = cell.x - x;
        int dy = cell.y - y;
        int dz = cell.z - z;

        if(Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1)
            throw new IllegalArgumentException("Distance should be exactly 1!");

        if(!linking) {
            linking = true;
            if (dx == 1) {
                if (east == null)
                    cell.link(this);
                east = cell;
            }
            if (dx == -1) {
                if (west == null)
                    cell.link(this);
                west = cell;
            }
            if (dy == 1) {
                if (north == null)
                    cell.link(this);
                north = cell;
            }
            if (dy == -1) {
                if (south == null)
                    cell.link(this);
                south = cell;
            }
            if (dz == 1) {
                if (up == null)
                    cell.link(this);
                up = cell;
            }
            if (dz == -1) {
                if (down == null)
                    cell.link(this);
                down = cell;
            }
        }
        linking = false;
    }

    public Cell fetch(int x, int y, int z) {

        if(x == 0 && y == 0 && z == 0)
            return this;

        if(Math.abs(x) + Math.abs(y) + Math.abs(z) == 1) {
            if(x == 1)
                return east;
            else if(x == -1)
                return west;
            else if(y == 1)
                return north;
            else if(y == -1)
                return south;
            else if(z == 1)
                return up;
            else
                return down;
        } else {
            if(Math.abs(y) >= Math.abs(x)) {
                if(Math.abs(y) >= Math.abs(z))
                    return (Integer.signum(y) == 1 ? north : south).fetch(x, y - Integer.signum(y), z);
                else
                    return (Integer.signum(z) == 1 ? up : down).fetch(x, y, z - Integer.signum(z));
            }
            else
                return (Integer.signum(x) == 1 ? east : west).fetch(x - Integer.signum(x), y, z);
        }
    }

    public Cell fetch(Cell cell) {
        return fetch(cell.getX(), cell.getY(), cell.getZ());
    }

    public int distanceTo(Cell cell) {
        return Math.abs(x - cell.getX()) + Math.abs(y - cell.getY()) + Math.abs(z - cell.getZ());
    }

    public Location getLocation() {
        return new Location(x, y, z);
    }

    /*public Location add(Location loc) {
        return new Location(x + loc.x, y + loc.y, z + loc.z);
    }

    public Location subtract(Location loc) {
        return new Location(x - loc.x, y - loc.y, z - loc.z);
    }

    public Location add(int xIncr, int yIncr, int zIncr) {
        return new Location(x + xIncr, y + yIncr, z + zIncr);
    }*/

    @Override
    public String toString() {
        return "Location(" + x + "," + y + "," + z + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Cell) {
            Cell cell = (Cell)obj;
            return (x == cell.x) && (y == cell.y) && (z == cell.z);
        }
        return false;
    }
}
