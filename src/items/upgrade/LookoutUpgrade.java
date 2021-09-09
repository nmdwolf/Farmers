package items.upgrade;

import core.GameConstants;
import core.Player;
import core.Resource;
import items.GameObject;
import items.buildings.MainBuilding;
import items.units.Unit;

import java.util.HashMap;

public class LookoutUpgrade extends Upgrade<MainBuilding> {

    public final static int LOOKOUT_ID = 1000;

    public final static int FOOD_COST = -100;
    public final static int WATER_COST = -100;
    public final static int WOOD_COST = -100;
    public final static int CYCLE_THRESHOLD = 10;

    public LookoutUpgrade(Player p) {
        super(p, new HashMap<>(){{
            put(Resource.FOOD, FOOD_COST);
            put(Resource.WATER, WATER_COST);
            put(Resource.WOOD, WOOD_COST);
        }}, CYCLE_THRESHOLD);
    }

    @Override
    public void upgrade() {
        super.upgrade();
        getPlayer().unlockView();
        getPlayer().getObjects().stream().filter(obj -> obj.getType().equals("Base")).forEach(b -> applyTo((MainBuilding) b.getObject(GameConstants.CONSTRUCTABLE_TYPE)));
    }

    @Override
    public void applyTo(MainBuilding object) {
        object.changeLineOfSight(1);
        for(int x = -2; x < 3; x++)
            for (int y = -2; y < 3; y++)
                if(Math.abs(x) + Math.abs(y) == 2)
                    object.getPlayer().spot(object.getLocation().add(x, y, 0));
    }

    @Override
    public int getID() {
        return LOOKOUT_ID;
    }

    @Override
    public void notifyObserver(GameObject... objects) {
        for(GameObject object : objects)
            if(object instanceof MainBuilding)
                applyTo((MainBuilding) object);
    }

    @Override
    public String toString() {
        return "Lookout";
    }
}
