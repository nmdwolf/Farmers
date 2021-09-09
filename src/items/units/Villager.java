package items.units;

import static core.Options.*;
import core.GameConstants;
import core.Location;
import core.Player;
import core.Resource;
import items.Constructable;
import items.buildings.House;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Villager extends ConstructiveUnit {

    public final static int VILLAGER_HEALTH = 1000;
    public final static int VILLAGER_ENERGY = 20;
    public final static int VILLAGER_SIZE = 1;
    public final static int VILLAGER_SIGHT = 1;

    public final static int VILLAGER_FOOD = -100;
    public final static int VILLAGER_WATER = -100;

    public final static int DEGRADATION_CYCLE = 50;
    public final static int DEGRADATION_AMOUNT = 2;

    public Villager(Player p, Location loc) {
        super(p, loc, new HashMap<>() {{
            put(Resource.FOOD, VILLAGER_FOOD);
            put(Resource.WATER, VILLAGER_WATER);
        }}, new HashMap<>() {{
            put(HEALTH_KEY, VILLAGER_HEALTH);
            put(STATUS_KEY, GameConstants.ACTIVE_KEY);
            put(ENERGY_KEY, VILLAGER_ENERGY);
            put(SIGHT_KEY, VILLAGER_SIGHT);
            put(SIZE_KEY, VILLAGER_SIZE);
            put(DEGRADATION_AMOUNT_KEY, DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE_KEY, DEGRADATION_CYCLE);
        }});
    }

    @Override
    public String getType() {
        return "Villager";
    }

    @Override
    public String getToken() {
        return "v";
    }

    @Override
    public List<Constructable> getProducts() {
        ArrayList<Constructable> products = new ArrayList<>();
        products.add(new House(getPlayer(), getLocation()));
        return products;
    }

    @Override
    public boolean isVisible(int cycle) {
        return true;
    }
}