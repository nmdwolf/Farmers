package objects.buildings;

import core.*;
import general.CustomMethods;
import general.OperationsList;
import objects.resources.Resource;
import objects.resources.ResourceContainer;

import java.awt.image.BufferedImage;

public class Stable extends ConstructiveBuilding {

    public final static ResourceContainer STABLE_COST = new ResourceContainer() {{
        put(Resource.FOOD, -100);
        put(Resource.WATER, -200);
        put(Resource.WOOD, -200);
        put(Resource.TIME, 10);
    }};
    public final static Award BUILT_AWARD = new Award(CustomMethods.getNewAwardIdentifier(), "Yee-haw!");

    public final static int STABLE_HEALTH = 500;
    public final static int STABLE_SPACE = 3;
    public final static int STABLE_SIGHT = 1;
    public final static int STABLE_DIFFICULTY = 1;

    public final static int STABLE_DEGRADATION_TIME = 50;
    public final static int STABLE_DEGRADATION_AMOUNT = 2;
    public final static int STABLE_X = 0;
    public final static int STABLE_Y = 0;

    public final static String TOKEN = "S";

    public Stable(Player p, Cell cell, int cycle) {
        super(p, cell, cycle, STABLE_SPACE, STABLE_SIGHT, STABLE_HEALTH,
                STABLE_DEGRADATION_TIME, STABLE_DEGRADATION_AMOUNT, STABLE_COST, STABLE_DIFFICULTY,
                STABLE_X, STABLE_Y);
    }

    @Override
    public String getClassLabel() {
        return "Stable";
    }

    @Override
    public String getToken() {
        return TOKEN;
    }

    @Override
    public Award getConstructionAward() {
        return BUILT_AWARD;
    }

    @Override
    public BufferedImage getSprite() {
        return null;
    }

    @Override
    public Award getEvolveAward() {
        return null;
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        return new OperationsList();
    }

    @Override
    public void cycle(int cycle) {

    }
}
