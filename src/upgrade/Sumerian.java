package upgrade;

import core.Civilization;
import core.Player;
import resources.Resource;
import general.CustomMethods;
import resources.ResourceContainer;
import items.GameObject;

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

    private class WritingUpgrade extends Upgrade {

        public final static int WRITING_ID = CustomMethods.getNewUpgradeIdentifier();

        public final static int CYCLE_THRESHOLD = 10;

        public final static ResourceContainer WRITING_COST = new ResourceContainer() {{
            put(Resource.WOOD, -100);
        }};

        public WritingUpgrade() {
            super(player, WRITING_COST, CYCLE_THRESHOLD);
        }

        @Override
        public void apply(GameObject object) {
            object.changeMaxHealth(10);
        }

        @Override
        public int getID() {
            return WRITING_ID;
        }
    }
}
