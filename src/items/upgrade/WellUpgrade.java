package items.upgrade;

import core.Player;
import core.Resource;
import items.buildings.MainBuilding;
import items.sources.SourceDecorator;

import java.util.HashMap;

public class WellUpgrade extends InstanceUpgrade<MainBuilding> {

    public final static int WELL_ID = 1002;
    public final static int WATER_SOURCE = 5;

    public final static int FOOD_COST = -200;
    public final static int WATER_COST = -100;
    public final static int CYCLE_THRESHOLD = 10;

    public WellUpgrade(Player p, MainBuilding obj) {
        super(p, new HashMap<>(){{
            put(Resource.FOOD, FOOD_COST);
            put(Resource.WATER, WATER_COST);
        }}, CYCLE_THRESHOLD, obj);
    }

    @Override
    public void applyTo(MainBuilding object) {
        getPlayer().removeObject(object);
        HashMap<Resource, Integer> resources = new HashMap<>();
        resources.put(Resource.WATER, WATER_SOURCE);
        getPlayer().addObject(new SourceDecorator(object, resources));
    }

    @Override
    public int getID() {
        return WELL_ID;
    }

    @Override
    public String toString() {
        return "Build well";
    }

}
