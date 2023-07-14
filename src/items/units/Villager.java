package items.units;

import static core.Option.*;

import core.*;
import core.contracts.ConstructContract;
import general.CustomMethods;
import general.OperationsList;
import general.ResourceContainer;
import items.buildings.House;
import items.buildings.Lumberjack;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

public class Villager extends Worker {

    public final static BufferedImage sprite = CustomMethods.getSprite("src/img/villager.png", GameConstants.UNIT_SPRITE_SIZE, GameConstants.UNIT_SPRITE_SIZE);
    public final static BufferedImage workingSprite = CustomMethods.getSprite("src/img/villager_working.png", GameConstants.UNIT_SPRITE_SIZE, GameConstants.UNIT_SPRITE_SIZE);
    public final static List<Resource> resources = List.of(Resource.FOOD, Resource.WOOD, Resource.STONE, Resource.COAL, Resource.IRON);

    public final static int VILLAGER_HEALTH = 100;
    public final static int VILLAGER_ENERGY = 5;
    public final static int VILLAGER_SIZE = 1;
    public final static int VILLAGER_SIGHT = 1;
    public final static int VILLAGER_ANIMATION = 1000;

    public final static ResourceContainer VILLAGER_COST = new ResourceContainer() {{
        put(Resource.FOOD, -100);
        put(Resource.WATER, -100);
        put(Resource.TIME, 1);
    }};

    public final static int VILLAGER_DEGRADATION_CYCLE = 50;
    public final static int VILLAGER_DEGRADATION_AMOUNT = 2;

    public Villager(Player p, Cell cell) {
        super(p, cell, VILLAGER_SIZE, VILLAGER_COST, new HashMap<>() {{
            put(MAX_HEALTH, VILLAGER_HEALTH);
            put(STATUS, GameConstants.IDLE_STATUS);
            put(MAX_ENERGY, VILLAGER_ENERGY);
            put(SIGHT, VILLAGER_SIGHT);
            put(SIZE, VILLAGER_SIZE);
            put(DEGRADATION_AMOUNT, VILLAGER_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE, VILLAGER_DEGRADATION_CYCLE);
            put(ANIMATION, VILLAGER_ANIMATION);
            put(HUNT, 5);
            put(DRINK, 5);
            put(LOG, 5);
            put(MASON, 5);
            put(WELD, 0);
            put(MINE, 0);
        }});
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
    public List<Resource> getResources() {
        return resources;
    }

    @Override
    public boolean checkStatus(Option option) {
        if(option == ENABLED)
            return true;
        else
            return super.checkStatus(option);
    }

    @Override
    public OperationsList getOperations(Option... options) {
        OperationsList operations =  super.getOperations(options);
        for(Option option : options) {
            if (option == CONSTRUCT) {
                operations.put("House", () -> {
                    addContract(new ConstructContract(Villager.this, new House(getPlayer(), getCell())));
                });
                operations.put("Lumberjack", () -> {
                    addContract(new ConstructContract(Villager.this, new Lumberjack(getPlayer(), getCell())));
                });
            }
        }
        return operations;
    }

    @Override
    public BufferedImage getSprite() {
        if(getValue(STATUS) != GameConstants.WORKING_STATUS)
            return sprite;
        else
            return workingSprite;
    }

    @Override
    public Award getAward(Option option) {
        return null;
    }
}