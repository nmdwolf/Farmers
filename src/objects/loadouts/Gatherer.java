package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import core.resources.ResourceContainer;

public class Gatherer extends Loadout {

    private ResourceContainer yield;
    private int energyCost;

    @JsonCreator
    private Gatherer(ResourceContainer yield, int energyCost) {
        super("gatherer");
        this.yield = yield;
        this.energyCost = energyCost;
    }

    public int getYield(String res) {
        return yield.get(res);
    }

    public int getEnergyCost() {
        return energyCost;
    }

    @Override
    public String toString() {
            return "Yield: {" + yield + "}";
    }

}
