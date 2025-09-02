package core.upgrade;

import core.player.Player;
import core.resources.Resource;
import core.resources.ResourceContainer;
import UI.CustomMethods;
import objects.GameObject;
import objects.units.Unit;

public class LeatherUpgrade extends Upgrade {

    public final static int LEATHER_ID = CustomMethods.getNewUpgradeIdentifier();
    public final static int HEALTH_CHANGE = 20;

    public final static int CYCLE_THRESHOLD = 10;

    public final static ResourceContainer LEATHER_COST = new ResourceContainer() {{
        put(Resource.FOOD, -200);
        put(Resource.WATER, -100);
    }};

    public LeatherUpgrade(Player player) {
        super(player, LEATHER_COST, CYCLE_THRESHOLD);
    }

    @Override
    public int getID() { return LEATHER_ID; }

    @Override
    public void apply(GameObject object) {
        if(object instanceof Unit)
            object.changeMaxHealth(HEALTH_CHANGE);
    }

    @Override
    public String toString() { return "Leather"; }
}
