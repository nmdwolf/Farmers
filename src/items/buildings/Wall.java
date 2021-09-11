package items.buildings;

import core.*;
import items.GameObject;
import items.upgrade.EvolveUpgrade;
import items.upgrade.LookoutUpgrade;
import items.upgrade.Upgrade;

import java.util.HashMap;
import java.util.List;

import static core.Options.*;

public class Wall extends Building {

    public final static ResourceContainer WALL_COST = new ResourceContainer() {{
        put(Resource.WOOD, -200);
    }};

    public final static int WALL_HEALTH = 500;
    public final static int WALL_SPACE = 0;
    public final static int WALL_SIZE = 5;
    public final static int WALL_SIGHT = 1;

    public final static int WALL_DEGRADATION_CYCLE = 50;
    public final static int WALL_DEGRADATION_AMOUNT = 5;

    public Wall(Player player, Location loc) {
        super(player, loc, WALL_COST, new HashMap<>() {{
            put(HEALTH_KEY, WALL_HEALTH);
            put(STATUS_KEY, GameConstants.FOUNDATION_KEY);
            put(SPACE_KEY, WALL_SPACE);
            put(SIGHT_KEY, WALL_SIGHT);
            put(SIZE_KEY, WALL_SIZE);
            put(DEGRADATION_AMOUNT_KEY, WALL_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE_KEY, WALL_DEGRADATION_CYCLE);
        }});
        updateDescriptions(Type.OBSTRUCTION_TYPE);
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
    public boolean checkStatus(Options option) {
        return option == ENABLED_KEY ? getPlayer().hasUpgrade(new LookoutUpgrade(getPlayer())) : super.checkStatus(option);
    }

    @Override
    public List<GameObject> getProducts() {
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
}
