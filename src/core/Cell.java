package core;

import core.resources.ResourceContainer;
import objects.GameObject;
import objects.Obstruction;
import objects.buildings.Building;
import objects.buildings.Directional;
import objects.units.Unit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static core.GameConstants.*;

// TODO Fix North/South (y coordinates go down on computers)
public class Cell {

    private final ResourceContainer resources;
    private final Location loc;
    private final HashSet<GameObject<?>> content;
    private int unitSpace, unitOccupied, buildingSpace, buildingOccupied, travelCost, heatLevel, traversalCount;

    private Cell east, west, north, south, up, down;

    public Cell(int x, int y, int z, int s, int b) {
        this.loc = new Location(x, y, z);
        this.content = new HashSet<>();
        unitSpace = s;
        buildingSpace = b;
        travelCost = INITIAL_TRAVEL_COST;
        traversalCount = 0;
        resources = generateResources();
        seasonalCycle(0); // Start in Winter
    }

    public void cycle(int cycle) {
        if(cycle % SEASON_LENGTH == 0)
            seasonalCycle((cycle % (4 * SEASON_LENGTH)) / SEASON_LENGTH);
        if(heatLevel >= HOT_LEVEL)
            resources.put("Water", Math.max(resources.get("Water") - 2, 0));
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

    /**
     * Calculates the energy cost for passing through this cell based on the inbound direction.
     * @param direction inbound direction
     * @return energy cost
     */
    public int getTravelCost(Direction direction) {
        // basic cost
        int cost = travelCost;

        // if "river"
        if(isRiver())
            cost += 2;

        // if "hot"
        cost += Math.max(0, heatLevel - HOT_LEVEL);

        // Obstructions in this cell
        var obstructions = content.stream().filter(Obstruction.class::isInstance).map(Obstruction.class::cast).filter(Obstruction::isActive);
        for(Obstruction ob : obstructions.collect(Collectors.toSet())) {
            if(ob instanceof Directional dir)
                cost += dir.getDirection().equals(direction) ? ob.getObstructionCost() : 0;
            else
                cost += ob.getObstructionCost();
        }

        // Obstructions in source cell
        obstructions = fetch(Direction.toDisplacement(direction)).content.stream().filter(Obstruction.class::isInstance).map(Obstruction.class::cast).filter(Obstruction::isActive);
        for(Obstruction ob : obstructions.collect(Collectors.toSet())) {
            if(ob instanceof Directional dir)
                cost += dir.getDirection().equals(direction.opposite()) ? ob.getObstructionCost() : 0;
            else
                cost += ob.getObstructionCost();
        }

        return cost;
    }

    public void changeTravelCost(int amount) {
        travelCost += amount;
    }

    public int getResource(String type) {
        return resources.get(type);
    }

    public int changeResource(String type, int amount) {

        if(amount < 0)
            amount = -Math.min(resources.get(type), -amount);

        if(type.equals("Water"))
            if(heatLevel >= 50 || heatLevel <= COLD_LEVEL)
                amount = 0;

        resources.put(type, resources.get(type) + amount);
        return amount;
    }

    public void changeResources(ResourceContainer res) {
        for(String resource : res.keySet())
            changeResource(resource, res.get(resource));
    }

    public int getHeatLevel() { return heatLevel; }

    public void changeHeatLevel(int amount) { heatLevel += amount; }

    public boolean isRiver() { return resources.get("Water") >= WATER_THRESHOLD; }

    public boolean isForest() { return resources.get("Wood") >= WOOD_THRESHOLD; }

    public boolean isField() { return resources.get("Food") >= FOOD_THRESHOLD; }

    /**
     * Indicates whether this cell contains a natural road, i.e. whether sufficiently many units have moved through this cell (based on the constant {@code GameConstants.ROAD_THRESHOLD}.
     * @return if this is a road
     */
    public boolean isRoad() {
        return traversalCount >= ROAD_THRESHOLD;
    }

    public void addContent(GameObject<?> obj) {
        if(!equals(obj.getCell())) {
            content.add(obj);
            if (obj instanceof Unit) {
                traversalCount++;
                changeUnitOccupied(obj.getSize());
            } else if (obj instanceof Building)
                changeBuildingOccupied(obj.getSize());
        } else
            throw new IllegalArgumentException("Object is already located in this cell.");
    }

    public void removeContent(GameObject<?> obj) {
        if(equals(obj.getCell())) {
            content.remove(obj);
            if (obj instanceof Unit)
                changeUnitOccupied(-obj.getSize());
            else if (obj instanceof Building)
                changeBuildingOccupied(-obj.getSize());
        } else
            throw new IllegalArgumentException("Object is not located in this cell.");
    }

    public HashSet<GameObject<?>> getObjects() { return content; }

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
                resources.put("Water", resources.get("Water") + rand.nextInt(30));
        }
        else if(season == 2)
            heatLevel += rand.nextInt(5) + 1;
        else if(season == 3) {
            heatLevel += rand.nextInt(3) - 1;
            if(rand.nextInt(5) >= 2)
                resources.put("Water", resources.get("Water") + rand.nextInt(50));
        }
    }

    public int getX() {
        return loc.x();
    }

    public int getY() {
        return loc.y();
    }

    public int getZ() {
        return loc.z();
    }

    public void link(Cell cell) {
        Location diff = cell.getLocation().add(loc.negative());
        int dx = diff.x();
        int dy = diff.y();
        int dz = diff.z();

        if(Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1)
            throw new IllegalArgumentException("Distance should be exactly 1!");

        if (dx == 1)
            east = cell;
        if (dx == -1)
            west = cell;
        if (dy == 1)
            north = cell;
        if (dy == -1)
            south = cell;
        if (dz == 1)
            up = cell;
        if (dz == -1)
            down = cell;
    }

    @NotNull
    public Cell fetch(int x, int y, int z) throws NullPointerException {
        if(x == 0 && y == 0 && z == 0)
            return this;

        if(!GAME_3D && z != 0)
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
            else if(GAME_3D && z == 1)
                return up;
            else if (GAME_3D)
                return down;
        } else {
            if(Math.abs(y) >= Math.abs(x)) {
                if(Math.abs(y) >= Math.abs(z))
                    return (Integer.signum(y) == 1 ? north : south).fetch(x, y - Integer.signum(y), z);
                else if(GAME_3D)
                    return (Integer.signum(z) == 1 ? up : down).fetch(x, y, z - Integer.signum(z));
            }
            else
                return (Integer.signum(x) == 1 ? east : west).fetch(x - Integer.signum(x), y, z);
        }

        throw new NullPointerException("Cell does not exist.");
    }

    @NotNull
    public Cell fetch(Location location) {
        return fetch(location.x(), location.y(), location.z());
    }

    public int distanceTo(Cell cell) {
        return Location.distance(cell.getLocation(), loc);
    }

    public Location getLocation() {
        return loc;
    }

    public boolean isEndOfMap() {
        return loc.x() == 0 || loc.x() == NUMBER_OF_CELLS - 1 || loc.y() == 0 || loc.y() == NUMBER_OF_CELLS - 1;
    }

    /**
     * Returns all neighbouring cells that are not map boundaries.
     * @return nonboundary neighbours
     */
    public Cell[] getNeighbours() {
        return Stream.of(north, east, south, west).filter(c -> !c.isEndOfMap()).toArray(Cell[]::new);
    }

    public Cell getNeighbour(Direction direction) {
        return switch(direction) {
            case NORTH -> south;
            case EAST -> east;
            case SOUTH -> north;
            case WEST -> west;
        };
    }

    @Override
    public String toString() {
        return "Location(" + loc.x() + ", " + loc.y() + ", " + loc.z() + ")";
    }

    public String getDescription() {
        StringBuilder description = new StringBuilder();
        for(String res : resources.keySet())
            if(!res.equals("Time"))
                description.append(res).append(": ").append(resources.get(res)).append("\n");

        description.append("\n\n").append("Heat level: ").append(heatLevel);

        return description.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Cell cell)
            return loc.equals(cell.loc);
        return false;
    }

    /**
     * Generates a random amount of {@code Resource}s to initialize a cell
     * @return HashMap with random amount of resources
     */
    private static ResourceContainer generateResources() {
        ResourceContainer resources = new ResourceContainer();
        resources.put("Food", rand.nextInt(200));
        resources.put("Wood", rand.nextInt(250));
        resources.put("Stone", rand.nextInt(100));
        resources.put("Iron", rand.nextInt(50));
        resources.put("Coal", rand.nextInt(50));
        resources.put("Time", 0);

        resources.put("Water", rand.nextInt(200));
        if(resources.get("Water") > GameConstants.WATER_THRESHOLD)
            resources.put("Water", 300 + rand.nextInt(100));

        return resources;
    }
}
