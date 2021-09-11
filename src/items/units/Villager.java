package items.units;

import static core.Options.*;

import core.*;
import items.GameObject;
import items.buildings.House;
import items.upgrade.EvolveUpgrade;
import items.upgrade.Upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Villager extends Worker {

    public final static int VILLAGER_HEALTH = 100;
    public final static int VILLAGER_ENERGY = 5;
    public final static int VILLAGER_SIZE = 1;
    public final static int VILLAGER_SIGHT = 1;

    public final static ResourceContainer VILLAGER_COST = new ResourceContainer(){{
        put(Resource.FOOD, -100);
        put(Resource.WATER, -100);
    }};

    public final static int DEGRADATION_CYCLE = 50;
    public final static int DEGRADATION_AMOUNT = 2;

    public Villager(Player p, Location loc) {
        super(p, loc, VILLAGER_COST, new HashMap<>() {{
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
    public List<GameObject> getProducts() {
        ArrayList<GameObject> products = new ArrayList<>();
        products.add(new House(getPlayer(), getLocation()));
        return products;
    }

    @Override
    public List<Upgrade> getUpgrades() {
        return null;
    }

    @Override
    public boolean checkStatus(Options option) {
        if(option == ENABLED_KEY)
            return true;
        else
            return super.checkStatus(option);
    }

    @Override
    public List<EvolveUpgrade> getEvolutions() {
        return null;
    }
}