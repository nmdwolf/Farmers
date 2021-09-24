package items.upgrade;

import core.Player;
import core.Resource;
import general.ResourceContainer;
import general.CustomMethods;
import items.buildings.MainBuilding;
import items.sources.SourceDecorator;

public class WellUpgrade extends InstanceUpgrade<MainBuilding> {

    public final static int WELL_ID = CustomMethods.getNewUpgradeIdentifier();
    public final static int WATER_SOURCE = 5;

    public final static int CYCLE_THRESHOLD = 10;

    public final static ResourceContainer WELL_COST = new ResourceContainer() {{
        put(Resource.WOOD, -100);
        put(Resource.WATER, -100);
    }};

    public WellUpgrade(Player p, MainBuilding obj) {
        super(obj, WELL_COST, CYCLE_THRESHOLD);
    }

    @Override
    public void applyTo(MainBuilding object) {
        getPlayer().removeObject(object);
        ResourceContainer resources = new ResourceContainer();
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
