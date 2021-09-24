package items.sources;

import core.*;
import general.ResourceContainer;

import java.util.HashMap;

public class Farm extends Source {

    public final static int FARM_SIZE = 1;
    public final static int FARM_SIGHT = 1;

    public final static int FARM_HEALTH = 200;
    public final static int FARM_DEGRADATION_CYCLE = 0;
    public final static int FARM_DEGRADATION_AMOUNT = 0;

    public Farm(Player p, Location loc) {
        super(p, loc, new HashMap<>() {{
            put(Option.SIZE, FARM_SIZE);
            put(Option.SIGHT, FARM_SIGHT);
            put(Option.STATUS, GameConstants.FOUNDATION_KEY);
            put(Option.MAX_HEALTH, FARM_HEALTH);
            put(Option.DEGRADATION_CYCLE, FARM_DEGRADATION_CYCLE);
            put(Option.DEGRADATION_AMOUNT, FARM_DEGRADATION_AMOUNT);
        }}, new ResourceContainer(10, 0, 0, 0, 0, 0));
    }

    @Override
    public String getClassIdentifier() {
        return "Farm";
    }

    @Override
    public String getToken() {
        return "F";
    }

}
