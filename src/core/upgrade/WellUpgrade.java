package core.upgrade;

import objects.resources.Resource;
import objects.resources.ResourceContainer;
import UI.CustomMethods;
import objects.buildings.TownHall;
import objects.resources.SourceDecorator;

public class WellUpgrade extends InstanceUpgrade<TownHall> {

    public final static int WELL_ID = CustomMethods.getNewUpgradeIdentifier();
    public final static int WATER_SOURCE = 5;

    public final static int CYCLE_THRESHOLD = 10;

    public final static ResourceContainer WELL_COST = new ResourceContainer() {{
        put(Resource.WOOD, -100);
        put(Resource.WATER, -100);
    }};

    public WellUpgrade(TownHall obj) {
        super(obj, WELL_COST, CYCLE_THRESHOLD);
    }

    @Override
    public void upgrade() {
        getPlayer().removeObject(getObject());
        getPlayer().addObject(new SourceDecorator(getObject(), Resource.WATER, WATER_SOURCE));
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
