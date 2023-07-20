package objects.units;

import core.*;
import core.contracts.ConstructContract;
import general.CustomMethods;
import general.OperationsList;
import objects.resources.Resource;
import objects.resources.ResourceContainer;
import objects.buildings.House;
import objects.buildings.Lumberjack;

import java.awt.image.BufferedImage;
import java.util.List;

public class Villager extends Worker {

    public final static BufferedImage sprite = CustomMethods.getSprite("src/img/villager.png", GameConstants.UNIT_SPRITE_SIZE, GameConstants.UNIT_SPRITE_SIZE);
    public final static BufferedImage workingSprite = CustomMethods.getSprite("src/img/villager_working.png", GameConstants.UNIT_SPRITE_SIZE, GameConstants.UNIT_SPRITE_SIZE);
    public final static Award BUILT_AWARD = new Award(CustomMethods.getNewAwardIdentifier(), "A baby was born.");

    public final static List<Resource> resources = List.of(Resource.FOOD, Resource.WOOD, Resource.STONE, Resource.COAL, Resource.IRON);

    public final static int VILLAGER_HEALTH = 100;
    public final static int VILLAGER_ENERGY = 5;
    public final static int VILLAGER_SIZE = 1;
    public final static int VILLAGER_SIGHT = 1;
    public final static int VILLAGER_ANIMATION = 1000;

    public final static ResourceContainer VILLAGER_COST = new ResourceContainer() {{
        put(Resource.FOOD, 100);
        put(Resource.WATER, 100);
        put(Resource.TIME, 1);
    }};
    public final static ResourceContainer VILLAGER_PRODUCTION = new ResourceContainer(5, 5, 5, 5, 0, 0);

    public final static int VILLAGER_DEGRADATION_TIME = 50;
    public final static int VILLAGER_DEGRADATION_AMOUNT = 2;

    public final static int VILLAGER_CYCLE_LENGTH = 12;

    public Villager(Player p, Cell cell, int cycle) {
        super(p, cell, cycle, VILLAGER_ANIMATION, VILLAGER_SIZE, VILLAGER_SIGHT, VILLAGER_HEALTH,
                VILLAGER_DEGRADATION_TIME, VILLAGER_DEGRADATION_AMOUNT, VILLAGER_CYCLE_LENGTH, VILLAGER_ENERGY,
                VILLAGER_COST, VILLAGER_PRODUCTION);
    }

    @Override
    public String getClassLabel() {
        return "Villager";
    }

    @Override
    public String getToken() {
        return "v";
    }

    @Override
    public OperationsList getOperations(int cycle) {
        OperationsList operations =  super.getOperations(cycle);
        operations.put("House", () -> addContract(new ConstructContract<>(Villager.this, new House(getPlayer(), getCell(), cycle))));
        operations.put("Lumberjack", () -> addContract(new ConstructContract<>(Villager.this, new Lumberjack(getPlayer(), getCell(), cycle))));
        return operations;
    }

    @Override
    public OperationsList getEvolutions(int cycle) {
        return new OperationsList();
    }

    @Override
    public Award getEvolveAward() {
        return null;
    }

    @Override
    public BufferedImage getSprite() {
        if(getStatus() != Status.WORKING)
            return sprite;
        else
            return workingSprite;
    }

    @Override
    public Award getConstructionAward() {
        return BUILT_AWARD;
    }
}