package core.upgrade;

import core.player.Player;
import UI.CustomMethods;
import objects.GameObject;
import objects.resources.Resource;
import objects.resources.ResourceContainer;

public class LookoutUpgrade extends Upgrade {

    public final static int LOOKOUT_ID = CustomMethods.getNewUpgradeIdentifier();

    public final static int CYCLE_THRESHOLD = 10;

    public final static ResourceContainer LOOKOUT_COST = new ResourceContainer() {{
        put(Resource.FOOD, -100);
        put(Resource.WATER, -100);
        put(Resource.WOOD, -100);
    }};

    public LookoutUpgrade(Player p) {
        super(p, LOOKOUT_COST, CYCLE_THRESHOLD);
    }

    @Override
    public void upgrade() {
        super.upgrade();
        getPlayer().unlockView();
    }

    @Override
    public void apply(GameObject object) {}

    @Override
    public int getID() {
        return LOOKOUT_ID;
    }

    @Override
    public String toString() {
        return "Lookout";
    }
}
