package items.buildings;

import static core.Options.*;

import core.*;
import items.Constructable;
import items.Upgrader;
import items.units.Villager;
import items.upgrade.LookoutUpgrade;
import items.upgrade.Upgrade;
import items.upgrade.WellUpgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainBuilding extends ConstructiveBuilding implements Upgrader {

    public final static int BASE_WOOD = -200;
    public final static int BASE_STONE = -200;

    public final static int BASE_HEALTH = 1000;
    public final static int BASE_SPACE = 5;
    public final static int BASE_SIZE = 5;
    public final static int BASE_SIGHT = 1;

    public final static int BASE_DEGRADATION_CYCLE = 50;
    public final static int BASE_DEGRADATION_AMOUNT = 1;

    public MainBuilding(Player p, Location loc) {
        super(p, loc, new HashMap<>() {{
            put(Resource.WOOD, BASE_WOOD);
            put(Resource.STONE, BASE_STONE);
        }}, new HashMap<>() {{
            put(HEALTH_KEY, BASE_HEALTH);
            put(STATUS_KEY, GameConstants.FOUNDATION_KEY);
            put(SPACE_KEY, BASE_SPACE);
            put(SIGHT_KEY, BASE_SIGHT);
            put(SIZE_KEY, BASE_SIZE);
            put(DEGRADATION_AMOUNT_KEY, BASE_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE_KEY, BASE_DEGRADATION_CYCLE);
        }});
        updateDescriptions(GameConstants.UPGRADER_TYPE);
    }

    @Override
    public String toString() {
        return "Base";
    }

    @Override
    public String getType() {
        return "Base";
    }

    @Override
    public String getToken() {
        return "Base";
    }

    @Override
    public List<Upgrade> getUpgrades() {
        ArrayList<Upgrade> upgrades = new ArrayList<>();
        upgrades.add(new LookoutUpgrade(getPlayer()));
        upgrades.add(new WellUpgrade(getPlayer(), this));
        return upgrades;
    }

    @Override
    public List<Constructable> getProducts() {
        ArrayList<Constructable> products = new ArrayList<>();
        products.add(new Villager(getPlayer(), getConstructionLocation()));
        return products;
    }

    @Override
    public void work() {}

    @Override
    public void addContract(Contract c) {}

    @Override
    public boolean hasContract() {
        return false;
    }

    @Override
    public boolean isVisible(int cycle) {
        return true;
    }
}
