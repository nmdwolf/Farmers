package items.sources;

import core.*;
import items.GameObject;
import items.upgrade.EvolveUpgrade;
import items.upgrade.Upgrade;

import java.util.List;

public class Farm extends GameObject {

    public final static int FARM_SIZE = 1;
    public final static int FARM_SIGHT = 1;

    private final ResourceContainer gain;

    private boolean primed;

    public Farm(Player p, Location loc) {
        super(p, loc, FARM_SIZE, FARM_SIGHT);

        updateDescriptions(Type.SOURCE_TYPE);

        gain = new ResourceContainer();
        gain.put(Resource.FOOD, 10);
    }

    @Override
    public String getType() {
        return "Farm";
    }

    @Override
    public String getToken() {
        return "F";
    }

    @Override
    public ResourceContainer getResources(Options option) {
        return option == Options.SOURCE_KEY ? gain : ResourceContainer.EMPTY_CONTAINER;
    }

    @Override
    public void perform(Options option) {
        if(option == Options.SOURCE_KEY)
            primed = true;
        else
            super.perform(option);
    }

    @Override
    public boolean checkStatus(Options option) {
        return (option == Options.SOURCE_KEY) && primed;
    }

    @Override
    public List<Upgrade> getUpgrades() {
        return null;
    }

    @Override
    public List<GameObject> getProducts() {
        return null;
    }

    @Override
    public List<EvolveUpgrade> getEvolutions() {
        return null;
    }
}
