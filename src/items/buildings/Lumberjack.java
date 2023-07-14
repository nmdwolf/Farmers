package items.buildings;

import core.*;
import general.ResourceContainer;
import items.Booster;
import items.GameObject;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import static core.Option.*;

public class Lumberjack extends Building implements Booster {

    public final static ResourceContainer LUMBERJACK_COST = new ResourceContainer() {{
        put(Resource.WOOD, -100);
        put(Resource.WATER, -50);
        put(Resource.TIME, 5);
    }};

    public final static int LUMBERJACK_HEALTH = 250;
    public final static int LUMBERJACK_SIZE = 1;
    public final static int LUMBERJACK_SIGHT = 1;

    public final static int LUMBERJACK_DEGRADATION_CYCLE = 30;
    public final static int LUMBERJACK_DEGRADATION_AMOUNT = 2;

    public Lumberjack(Player p, Cell cell) {
        super(p, cell, 1, LUMBERJACK_COST, 1, new HashMap<>() {{
            put(MAX_HEALTH, LUMBERJACK_HEALTH);
            put(SIGHT, LUMBERJACK_SIGHT);
            put(SIZE, LUMBERJACK_SIZE);
            put(CONSTRUCT, 1);
            put(DEGRADATION_AMOUNT, LUMBERJACK_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE, LUMBERJACK_DEGRADATION_CYCLE);
            put(LOG, 1);
        }});
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
    public BufferedImage getSprite() {
        return null;
    }

    @Override
    public Award getAward(Option option) {
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
}
