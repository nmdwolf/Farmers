package objects.loadouts;

import core.resources.ResourceContainer;
import objects.GameObject;

public class Booster extends Loadout implements objects.Booster {

    private final int radius;
    private final ResourceContainer yield;

    public Booster(int radius, ResourceContainer yield) {
        super("booster");
        this.radius = radius;
        this.yield = yield;
    }

    @Override
    public int getBoostRadius() {
        return radius;
    }

    @Override
    // TODO Why also a GameObject?
    public int getBoostAmount(GameObject obj, String res) {
        return yield.get(res);
    }

    @Override
    // TODO Why also a GameObject?
    public ResourceContainer getBoostAmount(GameObject obj) {
        return yield;
    }

    @Override
    public String toString() {
        return "Boost radius: " + radius + "\n" +
                "Boosts: " + yield;
    }
}
