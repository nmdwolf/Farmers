package items.upgrade;

import core.*;
import general.CustomMethods;
import general.ResourceContainer;
import items.GameObject;
import items.buildings.MainBuilding;

public class LookoutUpgrade extends Upgrade<MainBuilding> {

    public final static int LOOKOUT_ID = CustomMethods.getNewUpgradeIdentifier();

    public final static int CYCLE_THRESHOLD = 10;

    public final static ResourceContainer LOOKOUT_COST = new ResourceContainer() {{
        put(Resource.FOOD, -100);
        put(Resource.WATER, -100);
        put(Resource.WOOD, -100);
    }};

    public LookoutUpgrade(Player player) {
        super(player, LOOKOUT_COST, CYCLE_THRESHOLD);
    }

    @Override
    public void upgrade() {
        super.upgrade();
        getPlayer().unlockView();
        getPlayer().getObjects().stream().filter(obj -> obj.getToken().equals(MainBuilding.TOKEN)).forEach(b -> applyTo((MainBuilding) b));
    }

    @Override
    public void applyTo(MainBuilding object) {
        object.changeValue(Option.SIGHT, 1);
        for(int x = -2; x < 3; x++)
            for (int y = -2; y < 3; y++)
                if(Math.abs(x) + Math.abs(y) == 2)
                    object.getPlayer().spot(object.getCell().fetch(x, y, 0));
    }

    @Override
    public int getID() {
        return LOOKOUT_ID;
    }

    @Override
    public void notifyObserver(GameObject... objects) {
        for(GameObject object : objects)
            if(object.getToken().equals(MainBuilding.TOKEN))
                applyTo((MainBuilding) object);
    }

    @Override
    public String toString() {
        return "Lookout";
    }
}
