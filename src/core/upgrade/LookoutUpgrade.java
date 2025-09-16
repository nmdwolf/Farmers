package core.upgrade;

import core.player.Player;
import objects.GameObject;
import core.resources.ResourceContainer;

public class LookoutUpgrade extends Upgrade {

    public final static int CYCLE_THRESHOLD = 10;
    public final static ResourceContainer LOOKOUT_COST = new ResourceContainer(new String[]{"Food", "Water", "Time"}, new int[]{100, 100, 100});

    public LookoutUpgrade() {
        super(LOOKOUT_COST, CYCLE_THRESHOLD, "Lookout", true);
    }

    @Override
    public void upgrade(Player p) {
        super.upgrade(p);
        p.unlockView();
    }

    @Override
    public void apply(GameObject<?> object) {}
}
