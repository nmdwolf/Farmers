package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonProperty;
import core.resources.ResourceContainer;
import objects.GameObject;

public class Booster extends Loadout implements objects.Booster {

    private final int radius;
    private final ResourceContainer yield;

    public Booster(@JsonProperty(defaultValue = "1") int radius, @JsonProperty(required = true) ResourceContainer yield) {
        super("booster");
        this.radius = radius;
        this.yield = yield;
    }

    @Override
    public int getBoostRadius() {
        return radius;
    }

    @Override
    public int getBoostAmount(GameObject<?> obj, String res) {
        return yield.get(res);
    }

    @Override
    public String toString() {
        return "Boost radius: " + radius + "\n" +
                "Boosts: " + yield;
    }
}
