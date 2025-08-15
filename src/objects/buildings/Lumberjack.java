package objects.buildings;

import core.*;
import UI.CustomMethods;
import objects.resources.Resource;
import objects.resources.ResourceContainer;
import objects.Booster;
import objects.GameObject;

import java.awt.image.BufferedImage;

public class Lumberjack extends IdleBuilding implements Booster {

    public final static ResourceContainer LUMBERJACK_COST = new ResourceContainer() {{
        put(Resource.WOOD, -100);
        put(Resource.WATER, -50);
        put(Resource.TIME, 5);
    }};
    public final static Award BUILT_AWARD = new Award(CustomMethods.getNewAwardIdentifier(), "You figured out how to chop wood.");

    public final static int LUMBERJACK_HEALTH = 250;
    public final static int LUMBERJACK_SPACE = 1;
    public final static int LUMBERJACK_SIGHT = 1;

    public final static int LUMBERJACK_DEGRADATION_TIME = 30;
    public final static int LUMBERJACK_DEGRADATION_AMOUNT = 2;

    public Lumberjack(Player p, Cell cell, int cycle) {
        super(p, cell, cycle, LUMBERJACK_SPACE, LUMBERJACK_SIGHT, LUMBERJACK_HEALTH,
                LUMBERJACK_DEGRADATION_TIME, LUMBERJACK_DEGRADATION_AMOUNT, LUMBERJACK_COST, 1);
    }

    @Override
    public String getClassLabel() {
        return "Lumberjack";
    }

    @Override
    public String getToken() {
        return "L";
    }

    @Override
    public BufferedImage getSprite(boolean max) {
        return null;
    }

    @Override
    public int getBoostRadius() { return 2; }

    @Override
    public int getBoostAmount(GameObject obj, Resource res) {
        if(res == Resource.WOOD)
            return 2;
        else
            return 0;
    }

    @Override
    public Award getConstructionAward() {
        return BUILT_AWARD;
    }
}
