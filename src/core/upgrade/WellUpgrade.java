package core.upgrade;

import core.resources.ResourceContainer;
import UI.CustomMethods;
import objects.buildings.TownHall;
import core.resources.SourceDecorator;

public class WellUpgrade extends InstanceUpgrade<TownHall> {

    public final static int WELL_ID = CustomMethods.getNewUpgradeIdentifier();
    public final static int WATER_SOURCE = 5;
    public final static int CYCLE_THRESHOLD = 10;
    public final static ResourceContainer WELL_COST = new ResourceContainer(new String[]{"Wood", "Water"}, new int[]{100, 100});

    public WellUpgrade(TownHall obj) {
        super(obj, WELL_COST, CYCLE_THRESHOLD);
    }

    @Override
    public void upgrade() {
        getPlayer().removeObject(getObject());
        getPlayer().addObject(new SourceDecorator(getObject(), "Water", WATER_SOURCE));
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
