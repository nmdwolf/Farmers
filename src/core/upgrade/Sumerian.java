package core.upgrade;

import core.player.Civilization;
import core.player.Player;
import core.resources.ResourceContainer;
import objects.GameObject;

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
    public GameObject<?> initObject(GameObject<?> obj) {
        return obj;
    }

    private static class WritingUpgrade extends Upgrade {

        public final static int CYCLE_THRESHOLD = 10;
        public final static ResourceContainer WRITING_COST = new ResourceContainer("Wood", 100);

        public WritingUpgrade() {
            super(WRITING_COST, CYCLE_THRESHOLD, "Writing", true);
        }

        @Override
        public void apply(GameObject<?> object) {
            object.changeMaxHealth(10);
        }
    }
}
