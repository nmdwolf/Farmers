package items.buildings;

import core.GameConstants;
import core.Location;
import core.Player;
import core.Resource;
import items.upgrade.LookoutUpgrade;

import java.util.HashMap;

import static core.Options.*;

public class Wall extends Building {

    public final static int WALL_WOOD = -200;

    public final static int WALL_HEALTH = 500;
    public final static int WALL_SPACE = 0;
    public final static int WALL_SIZE = 5;
    public final static int WALL_SIGHT = 1;

    public final static int WALL_DEGRADATION_CYCLE = 50;
    public final static int WALL_DEGRADATION_AMOUNT = 5;

    public Wall(Player player, Location loc) {
        super(player, loc, new HashMap<>() {{
            put(Resource.WOOD, WALL_WOOD);
        }}, new HashMap<>() {{
            put(HEALTH_KEY, WALL_HEALTH);
            put(STATUS_KEY, GameConstants.FOUNDATION_KEY);
            put(SPACE_KEY, WALL_SPACE);
            put(SIGHT_KEY, WALL_SIGHT);
            put(SIZE_KEY, WALL_SIZE);
            put(DEGRADATION_AMOUNT_KEY, WALL_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE_KEY, WALL_DEGRADATION_CYCLE);
        }});
        updateDescriptions(GameConstants.OBSTRUCTION_TYPE);
    }

    @Override
    public String getType() {
        return "Wall";
    }

    @Override
    public String getToken() {
        return "||";
    }

    @Override
    public boolean isVisible(int cycle) {
        return getPlayer().hasUpgrade(new LookoutUpgrade(getPlayer()));
    }
}
