package objects.units;

import core.*;
import general.CustomMethods;
import general.OperationsList;
import objects.resources.Resource;
import objects.resources.ResourceContainer;
import objects.Evolvable;
import core.upgrade.EvolveUpgrade;

import java.awt.image.BufferedImage;

public class Scout extends Unit implements Evolvable {

    public final static Award BUILT_AWARD = new Award(CustomMethods.getNewAwardIdentifier(), "You can look for new lands.");
    public final static Award EVOLVE_AWARD = new Award(CustomMethods.getNewAwardIdentifier(), "Exploring will be even easier now");

    public final static int SCOUT_HEALTH = 100;
    public final static int SCOUT_ENERGY = 10;
    public final static int SCOUT_SPACE = 2;
    public final static int SCOUT_SIGHT = 2;
    public final static int SCOUT_ANIMATION = 300;

    public final static ResourceContainer SCOUT_COST = new ResourceContainer(){{
        put(Resource.FOOD, -100);
        put(Resource.WATER, -200);
        put(Resource.TIME, 0);
    }};
    public final static ResourceContainer LEVEL1_COST = new ResourceContainer(){{
        put(Resource.FOOD, -200);
        put(Resource.WATER, -200);
        put(Resource.TIME, 10);
    }};

    public final static int SCOUT_DEGRADATION_TIME = 50;
    public final static int SCOUT_DEGRADATION_AMOUNT = 2;

    public final static int SCOUT_CYCLE_LENGTH = 12;

    public Scout(Player p, Cell cell, int cycle) {
        super(p, cell, cycle, SCOUT_ANIMATION, SCOUT_SPACE, SCOUT_SIGHT, SCOUT_HEALTH,
                SCOUT_DEGRADATION_TIME, SCOUT_DEGRADATION_AMOUNT, SCOUT_CYCLE_LENGTH, SCOUT_ENERGY, SCOUT_COST);
    }

    @Override
    public String getClassLabel() {
        return "Scout";
    }

    @Override
    public String getToken() {
        return "s";
    }

    @Override
    public OperationsList getEvolutions(int cycle) {
        OperationsList operations =  new OperationsList();

        operations.putUpgrade("Evolve", new EvolveUpgrade<>(this, LEVEL1_COST, 0, () -> {
            changeSight(1);
            changeMaxEnergy(5);
            changeMaxHealth(20);
        }));

        return operations;
    }

    @Override
    public Award getEvolveAward() {
        if(getLevel() == 2)
            return EVOLVE_AWARD;
        return null;
    }

    @Override
    public OperationsList getOperations(int cycle) {
        return new OperationsList();
    }

    @Override
    public Award getConstructionAward() {
        return BUILT_AWARD;
    }

    @Override
    public BufferedImage getSprite() {
        return null;
    }
}
