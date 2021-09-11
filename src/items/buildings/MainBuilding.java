package items.buildings;

import static core.Options.*;

import core.*;
import items.GameObject;
import items.units.Villager;
import items.upgrade.EvolveUpgrade;
import items.upgrade.LookoutUpgrade;
import items.upgrade.Upgrade;
import items.upgrade.WellUpgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainBuilding extends ConstructiveBuilding {

    public final static ResourceContainer BUILD_RESOURCES = new ResourceContainer() {{
        put(Resource.WOOD, -200);
    }};
    public final static ResourceContainer LEVEL1_RESOURCES = new ResourceContainer() {{
        put(Resource.WOOD, -300);
        put(Resource.STONE, -100);
        put(Resource.WATER, -100);
    }};
    public final static ResourceContainer LEVEL2_RESOURCES = new ResourceContainer() {{
        put(Resource.WOOD, -300);
        put(Resource.STONE, -300);
        put(Resource.WATER, -200);
        put(Resource.IRON, -50);
    }};

    public final static int BASE_HEALTH = 1000;
    public final static int BASE_SPACE = 5;
    public final static int BASE_SIZE = 5;
    public final static int BASE_SIGHT = 1;

    public final static int BASE_DEGRADATION_CYCLE = 50;
    public final static int BASE_DEGRADATION_AMOUNT = 1;

    public final static String TOKEN = "Base";

    private int level;

    public MainBuilding(Player p, Location loc) {
        super(p, loc, BUILD_RESOURCES, new HashMap<>() {{
            put(HEALTH_KEY, BASE_HEALTH);
            put(STATUS_KEY, GameConstants.FOUNDATION_KEY);
            put(SPACE_KEY, BASE_SPACE);
            put(SIGHT_KEY, BASE_SIGHT);
            put(SIZE_KEY, BASE_SIZE);
            put(DEGRADATION_AMOUNT_KEY, BASE_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE_KEY, BASE_DEGRADATION_CYCLE);
        }});
        updateDescriptions(Type.UPGRADER_TYPE);
        updateDescriptions(Type.EVOLVABLE_TYPE);
    }

    @Override
    public String getType() {
        return switch(level) {
            case 0: yield "Bonfire";
            case 1: yield "Town Center";
            case 2: default: yield "Castle";
        };
    }

    @Override
    public String getToken() {
        return TOKEN;
    }

    @Override
    public List<Upgrade> getUpgrades() {
        ArrayList<Upgrade> upgrades = new ArrayList<>();
        upgrades.add(new LookoutUpgrade(getPlayer()));
        upgrades.add(new WellUpgrade(getPlayer(), this));
        return upgrades;
    }

    @Override
    public List<GameObject> getProducts() {
        ArrayList<GameObject> products = new ArrayList<>();
        products.add(new Villager(getPlayer(), getLocation().add(getValue(CONSTRUCT_X_KEY), getValue(CONSTRUCT_Y_KEY), 0)));
        return products;
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
        if(level < 2) {
            ArrayList<EvolveUpgrade> evolutions = new ArrayList<>();
            evolutions.add(new EvolveUpgrade<>(MainBuilding.this, level == 0 ? LEVEL1_RESOURCES : LEVEL2_RESOURCES, 0));
            return evolutions;
        } else
            return new ArrayList<>(0);
    }
}
