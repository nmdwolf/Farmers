package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.resources.ResourceContainer;

public class Gatherer extends Loadout implements objects.Gatherer {

    private ResourceContainer yield;
    private int energyCost;

    @JsonCreator
    private Gatherer(@JsonProperty(required = true) ResourceContainer yield, @JsonProperty(required = true) int energyCost) {
        super("gatherer");
        this.yield = yield;
        this.energyCost = energyCost;
    }

    @Override
    public int getYield(String res) {
        return yield.get(res);
    }

    @Override
    public int getGatherCost() {
        return energyCost;
    }

    @Override
    // TODO include Boosters
    public String toString() {
            return "Yield: {" + yield + "}";
    }

}
