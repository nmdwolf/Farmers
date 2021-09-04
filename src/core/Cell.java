package core;

import core.GameConstants;

import java.util.HashMap;
import java.util.Random;

public class Cell {

    public final static Random rand = new Random(123);

    private int unitSpace, unitOccupied, buildingSpace, buildingOccupied;
    private HashMap<Integer, Integer> resources;

    public Cell(int s, int b) {
        unitOccupied = buildingOccupied = 0;
        unitSpace = s;
        buildingSpace = b;
        resources = generateResources();
    }

    public int getUnitSpace() {
        return unitSpace;
    }

    public int getUnitOccupied() {
        return unitOccupied;
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

    public void changeBuildingSpace(int amount) {
        buildingSpace += amount;
    }

    public void changeBuildingOccupied(int amount) {
        buildingOccupied += amount;
    }

    public int getResource(int type) {
        return resources.get(type);
    }

    public void changeResource(int type, int amount) {
        resources.put(type, resources.get(type) + amount);
    }

    public static  HashMap<Integer, Integer> generateResources() {
        HashMap<Integer,Integer> resources = new HashMap<>();
        resources.put(GameConstants.FOOD, rand.nextInt(100));
        resources.put(GameConstants.WOOD, rand.nextInt(100));
        resources.put(GameConstants.STONE, rand.nextInt(100));
        resources.put(GameConstants.IRON, rand.nextInt(50));
        resources.put(GameConstants.COAL, rand.nextInt(50));

        resources.put(GameConstants.WATER, rand.nextInt(200));
        if(resources.get(GameConstants.WATER) > GameConstants.WATER_THRESHOLD) {
            resources.put(GameConstants.WATER, 50000);
        }
        return resources;
    }
}
