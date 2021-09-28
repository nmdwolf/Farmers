package core;

import general.ResourceContainer;

import static core.GameConstants.*;
import static core.Resource.*;

public class Cell {

    private int unitSpace, unitOccupied, buildingSpace, buildingOccupied, travelCost, heatLevel;
    private final ResourceContainer resources;

    public Cell(int s, int b) {
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
        if(type == WATER && heatLevel <= COLD_LEVEL)
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

        resources.put(WATER, rand.nextInt(200));
        if(resources.get(WATER) > GameConstants.WATER_THRESHOLD)
            resources.put(WATER, 300 + rand.nextInt(100));

        return resources;
    }
}
