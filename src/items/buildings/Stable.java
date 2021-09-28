package items.buildings;

import core.*;
import general.CustomMethods;
import general.ResourceContainer;
import items.GameObject;
import items.upgrade.EvolveUpgrade;
import items.upgrade.Upgrade;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

import static core.Option.*;

public class Stable extends ConstructiveBuilding {

    public final static ResourceContainer BUILD_RESOURCES = new ResourceContainer(-100, -200, -200, 0, 0, 0);
    public final static Award BUILT_AWARD = new Award(CustomMethods.getNewAwardIdentifier(), "Yee-haw!");

    public final static int STABLE_HEALTH = 500;
    public final static int STABLE_SIZE = 3;
    public final static int STABLE_SIGHT = 1;

    public final static int STABLE_DEGRADATION_CYCLE = 50;
    public final static int STABLE_DEGRADATION_AMOUNT = 2;

    public final static String TOKEN = "S";

    public Stable(Player p, Location loc) {
        super(p, loc, BUILD_RESOURCES, new HashMap<>() {{
            put(MAX_HEALTH, STABLE_HEALTH);
            put(SIGHT, STABLE_SIGHT);
            put(SIZE, STABLE_SIZE);
            put(DEGRADATION_AMOUNT, STABLE_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE, STABLE_DEGRADATION_CYCLE);
        }});
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
    public String getClassIdentifier() {
        return "Stable";
    }

    @Override
    public String getToken() {
        return TOKEN;
    }

    @Override
    public Award getAward(Option option) {
        return option == CONSTRUCT ? BUILT_AWARD : null;
    }

    @Override
    public BufferedImage getSprite() {
        return null;
    }
}
