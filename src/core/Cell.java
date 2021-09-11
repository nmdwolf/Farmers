package core;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static core.GameConstants.*;
import static core.Resource.*;

public class Cell {

    public final static Random rand = new Random(19970605);

    private int unitSpace, unitOccupied, buildingSpace, buildingOccupied, travelCost;
    private final HashMap<Resource, Integer> resources;

    public Cell(int s, int b) {
        unitSpace = s;
        buildingSpace = b;
        travelCost = INITIAL_TRAVEL_COST;
        resources = generateResources();
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
        return resources.get(WATER) > WATER_THRESHOLD ? travelCost + 2 : travelCost;
    }

    public void changeTravelCost(int amount) {
        travelCost += amount;
    }

    public int getResource(Resource type) {
        return resources.get(type);
    }

    public void changeResource(Resource type, int amount) {
        resources.put(type, resources.get(type) + amount);
    }

    public void changeResources(Map<Resource, Integer> res) {
        for(Resource resource : res.keySet())
            changeResource(resource, res.get(resource));
    }

    public boolean isRiver() { return resources.get(WATER) >= WATER_THRESHOLD; }

    public boolean isForest() { return resources.get(WOOD) >= WOOD_THRESHOLD; }

    /**
     * Generates a random amount of resources to initialize a cell
     * @return HashMap with random amount of resources
     */
    private static HashMap<Resource, Integer> generateResources() {
        HashMap<Resource,Integer> resources = new HashMap<>();
        resources.put(FOOD, rand.nextInt(100));
        resources.put(WOOD, rand.nextInt(250));
        resources.put(STONE, rand.nextInt(100));
        resources.put(IRON, rand.nextInt(50));
        resources.put(COAL, rand.nextInt(50));

        resources.put(WATER, rand.nextInt(200));
        if(resources.get(WATER) > GameConstants.WATER_THRESHOLD) {
            resources.put(WATER, 300);
        }
        return resources;
    }
}
