package items.upgrade;

import core.Player;
import core.Resource;
import items.GameObject;
import items.units.Unit;

import java.util.HashMap;

public class LeatherUpgrade extends Upgrade<Unit> {

    public final static int LEATHER_ID = 1001;
    public final static int HEALTH_CHANGE = 20;

    public final static int FOOD_COST = -200;
    public final static int WATER_COST = -100;
    public final static int CYCLE_THRESHOLD = 10;

    public LeatherUpgrade(Player p) {
        super(p, new HashMap<>(){{
            put(Resource.FOOD, FOOD_COST);
            put(Resource.WATER, WATER_COST);
        }}, CYCLE_THRESHOLD);
    }

    @Override
    public void upgrade() {
        super.upgrade();
        getPlayer().getObjects().stream().filter(obj -> obj instanceof Unit).forEach(u -> applyTo((Unit) u));
    }

    @Override
    public void applyTo(Unit object) {
        object.changeMaxHealth(HEALTH_CHANGE);
        object.changeMaxHealth(HEALTH_CHANGE);
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
