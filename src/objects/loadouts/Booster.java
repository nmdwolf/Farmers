package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.resources.ResourceContainer;
import objects.GameObject;

public class Booster extends Loadout {

    @JsonProperty
    private final int radius = 1;
    private final ResourceContainer yield;

    @JsonCreator
    public Booster(@JsonProperty(required = true, value = "yield") ResourceContainer yield) {
        super("booster");
        this.yield = yield;
    }

    public int getBoostRadius() {
        return radius;
    }

    public int getBoostAmount(GameObject obj, String res) {
        return yield.get(res);
    }

    public ResourceContainer getYield() {
        return yield;
    }

    @Override
    public String toString() {
        return "Boost radius: " + radius + "\n" +
                "Boosts: " + yield;
    }
}
