package items.upgrade;

import core.Options;
import core.Player;
import core.Resource;
import core.ResourceContainer;
import general.CustomMethods;
import items.GameObject;
import items.units.Unit;

import java.util.HashMap;

public class LeatherUpgrade extends Upgrade<Unit> {

    public final static int LEATHER_ID = CustomMethods.getNewUpgradeIdentifier();
    public final static int HEALTH_CHANGE = 20;

    public final static int CYCLE_THRESHOLD = 10;

    public final static ResourceContainer LEATHER_COST = new ResourceContainer() {{
        put(Resource.FOOD, -200);
        put(Resource.WATER, -100);
    }};

    public LeatherUpgrade(Player p) {
        super(p, LEATHER_COST, CYCLE_THRESHOLD);
    }

    @Override
    public void upgrade() {
        super.upgrade();
        getPlayer().getObjects().stream().filter(obj -> obj instanceof Unit).forEach(u -> applyTo((Unit) u));
    }

    @Override
    public void applyTo(Unit object) {
        object.changeValue(Options.MAX_HEALTH_KEY, HEALTH_CHANGE);
        object.changeValue(Options.HEALTH_KEY, HEALTH_CHANGE);
    }

    @Override
    public int getID() {
        return LEATHER_ID;
    }

    @Override
    public void notifyObserver(GameObject... objects) {
        for(GameObject object : objects)
            if(object instanceof Unit)
                applyTo((Unit) object);
    }

    @Override
    public String toString() {
        return "Leather";
    }
}
