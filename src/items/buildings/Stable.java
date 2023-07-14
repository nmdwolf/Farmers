package items.buildings;

import core.*;
import general.CustomMethods;
import general.ResourceContainer;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import static core.Option.*;

public class Stable extends ConstructiveBuilding {

    public final static ResourceContainer STABLE_COST = new ResourceContainer() {{
        put(Resource.FOOD, -100);
        put(Resource.WATER, -200);
        put(Resource.WOOD, -200);
        put(Resource.TIME, 10);
    }};
    public final static Award BUILT_AWARD = new Award(CustomMethods.getNewAwardIdentifier(), "Yee-haw!");

    public final static int STABLE_HEALTH = 500;
    public final static int STABLE_SIZE = 3;
    public final static int STABLE_SIGHT = 1;
    public final static int STABLE_DIFFICULTY = 1;

    public final static int STABLE_DEGRADATION_CYCLE = 50;
    public final static int STABLE_DEGRADATION_AMOUNT = 2;

    public final static String TOKEN = "S";

    public Stable(Player p, Cell cell) {
        super(p, cell, STABLE_SIZE, STABLE_COST, STABLE_DIFFICULTY, new HashMap<>() {{
            put(MAX_HEALTH, STABLE_HEALTH);
            put(SIGHT, STABLE_SIGHT);
            put(SIZE, STABLE_SIZE);
            put(DEGRADATION_AMOUNT, STABLE_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE, STABLE_DEGRADATION_CYCLE);
        }});
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
    public Award getAward(Option option) {
        return option == CONSTRUCT ? BUILT_AWARD : null;
    }

    @Override
    public BufferedImage getSprite() {
        return null;
    }
}
