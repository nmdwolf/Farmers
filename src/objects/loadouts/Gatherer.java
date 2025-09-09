package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import core.resources.ResourceContainer;

public class Gatherer extends Loadout {

    private ResourceContainer yield;

    @JsonCreator
    private Gatherer(ResourceContainer yield) {
        super("gatherer");
        this.yield = yield;
    }

    public int getYield(String res) {
        return yield.get(res);
    }

    @Override
    public String toString() {
            return "Yield: {" + yield + "}";
    }

}
