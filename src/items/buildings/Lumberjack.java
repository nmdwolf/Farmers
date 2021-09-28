package items.buildings;

import core.*;
import general.ResourceContainer;
import items.GameObject;
import items.upgrade.EvolveUpgrade;
import items.upgrade.Upgrade;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

import static core.Option.*;

public class Lumberjack extends Booster{

    public final static ResourceContainer LUMBERJACK_COST = new ResourceContainer() {{
        put(Resource.WOOD, -100);
        put(Resource.WATER, -50);
    }};

    public final static int LUMBERJACK_HEALTH = 250;
    public final static int LUMBERJACK_SIZE = 1;
    public final static int LUMBERJACK_SIGHT = 1;

    public final static int LUMBERJACK_DEGRADATION_CYCLE = 30;
    public final static int LUMBERJACK_DEGRADATION_AMOUNT = 2;

    public Lumberjack(Player p, Location loc) {
        super(p, loc, LUMBERJACK_COST, new HashMap<>() {{
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
    public String getClassIdentifier() {
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
    public List<Upgrade> getUpgrades() {
        return null;
    }

    @Override
    public List<EvolveUpgrade> getEvolutions() {
        return null;
    }

    @Override
    public Award getAward(Option option) {
        return null;
    }
}
