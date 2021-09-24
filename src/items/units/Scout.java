package items.units;

import core.*;
import general.ResourceContainer;
import items.GameObject;
import items.buildings.House;
import items.buildings.Stable;
import items.upgrade.EvolveUpgrade;
import items.upgrade.Upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static core.Option.*;

public class Scout extends Unit {

    public final static int SCOUT_HEALTH = 100;
    public final static int SCOUT_ENERGY = 10;
    public final static int SCOUT_SIZE = 2;
    public final static int SCOUT_SIGHT = 2;

    public final static ResourceContainer SCOUT_COST = new ResourceContainer(){{
        put(Resource.FOOD, -100);
        put(Resource.WATER, -200);
    }};
    public final static ResourceContainer LEVEL1_COST = new ResourceContainer(){{
        put(Resource.FOOD, -200);
        put(Resource.WATER, -200);
    }};

    public final static int SCOUT_DEGRADATION_CYCLE = 50;
    public final static int SCOUT_DEGRADATION_AMOUNT = 2;

    public Scout(Player p, Location loc) {
        super(p, loc, SCOUT_COST, new HashMap<>() {{
            put(MAX_HEALTH, SCOUT_HEALTH);
            put(STATUS, GameConstants.IDLE_STATUS);
            put(MAX_ENERGY, SCOUT_ENERGY);
            put(SIGHT, SCOUT_SIGHT);
            put(SIZE, SCOUT_SIZE);
            put(DEGRADATION_AMOUNT, SCOUT_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE, SCOUT_DEGRADATION_CYCLE);
        }});
    }

    @Override
    public String getClassIdentifier() {
        return "Scout";
    }

    @Override
    public String getToken() {
        return "s";
    }

    @Override
    public boolean checkStatus(Option option) {
        if(option == ENABLED)
            return getPlayer().hasConstructed(House.HOUSE_TOKEN);
        else
            return super.checkStatus(option);
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
        if(player.hasConstructed(Stable.TOKEN)) {
            ArrayList<EvolveUpgrade> evolutions = new ArrayList<>();
            evolutions.add(new EvolveUpgrade<>(this, LEVEL1_COST, 0, s -> {
                s.changeValue(SIGHT, 1);
                s.changeValue(MAX_ENERGY, 5);
                s.changeValue(MAX_HEALTH, 20);
                s.changeValue(DEGRADATION_AMOUNT, 1);
                s.changeValue(CYCLE, 0);
            }));
            return evolutions;
        }
        return null;
    }
}
