package core.upgrade;

import core.resources.ResourceContainer;
import UI.CustomMethods;
import objects.buildings.TownHall;
import objects.loadouts.LoadoutFactory;

import java.util.HashMap;

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
        getObject().addLoadout(LoadoutFactory.createLoadout("gatherer", new HashMap<>() {{
            put("resources", new String[]{"Water"});
            put("amounts", new int[]{WATER_SOURCE});
        }}));
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
