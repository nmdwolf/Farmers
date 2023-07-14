package items.upgrade;

import core.Civilization;
import core.Option;
import core.Player;
import core.Resource;
import general.CustomMethods;
import general.ResourceContainer;
import items.GameObject;
import items.units.Unit;

import java.util.ArrayList;
import java.util.List;

public class Sumerian implements Civilization {

    private final Player player;
    private final ArrayList<Upgrade> upgrades;

    public Sumerian(Player player) {
        this.player = player;
        upgrades = new ArrayList<>();
        upgrades.add(new WritingUpgrade());
    }

    @Override
    public String getName() {
        return "Sumerian";
    }

    @Override
    public List<Upgrade> getUpgrades() {
        return upgrades;
    }

    @Override
    public GameObject initObject(GameObject obj) {
        return obj;
    }

    private class WritingUpgrade extends Upgrade<Unit> {

        public final static int WRITING_ID = CustomMethods.getNewUpgradeIdentifier();

        public final static int CYCLE_THRESHOLD = 10;

        public final static ResourceContainer WRITING_COST = new ResourceContainer() {{
            put(Resource.WOOD, -100);
        }};

        public WritingUpgrade() {
            super(player, WRITING_COST, CYCLE_THRESHOLD);
        }

        @Override
        public void applyTo(Unit object) {
            object.changeValue(Option.MAX_HEALTH, object.getValue(Option.MAX_HEALTH) + 10);
        }

        @Override
        public int getID() {
            return WRITING_ID;
        }

        @Override
        public void notifyObserver(GameObject... objects) {
            for(GameObject object : objects)
                if(object instanceof Unit)
                    applyTo((Unit) object);
        }
    }
}
