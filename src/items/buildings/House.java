package items.buildings;

import core.GameConstants;
import core.Location;
import core.Player;
import core.Resource;
import items.Upgrader;
import items.upgrade.LeatherUpgrade;
import items.upgrade.Upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static core.Options.*;
import static core.Options.SIZE_KEY;

public class House extends Building implements Upgrader {

    public final static int HOUSE_WOOD = -50;

    public final static int HOUSE_HEALTH = 150;
    public final static int HOUSE_SPACE = 3;
    public final static int HOUSE_SIZE = 1;
    public final static int HOUSE_SIGHT = 1;

    public final static int HOUSE_DEGRADATION_CYCLE = 20;
    public final static int HOUSE_DEGRADATION_AMOUNT = 2;

    public House(Player p, Location loc) {
        super(p, loc, new HashMap<>() {{
            put(Resource.WOOD, HOUSE_WOOD);
        }}, new HashMap<>() {{
            put(HEALTH_KEY, HOUSE_HEALTH);
            put(STATUS_KEY, GameConstants.FOUNDATION_KEY);
            put(SPACE_KEY, HOUSE_SPACE);
            put(SIGHT_KEY, HOUSE_SIGHT);
            put(SIZE_KEY, HOUSE_SIZE);
            put(DEGRADATION_AMOUNT_KEY, HOUSE_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE_KEY, HOUSE_DEGRADATION_CYCLE);
        }});
    }

    @Override
    public String toString() {
        return "House";
    }

    @Override
    public String getType() {
        return "House";
    }

    @Override
    public String getToken() {
        return "H";
    }

    @Override
    public List<Upgrade> getUpgrades() {
        ArrayList<Upgrade> upgrades = new ArrayList<>();
        upgrades.add(new LeatherUpgrade(getPlayer()));
        return upgrades;
    }

    @Override
    public boolean isVisible(int cycle) {
        return true;
    }
}
