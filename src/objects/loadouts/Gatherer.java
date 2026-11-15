package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.Grid;
import core.resources.ResourceContainer;

import java.util.Objects;

public class Gatherer extends Loadout {

    private ResourceContainer yield;
    private int energyCost;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    private Gatherer(@JsonProperty(required = true, value = "yield") ResourceContainer yield,
                     @JsonProperty(required =
            true, value = "energyCost") int energyCost) {
        super("gatherer");
        this.yield = yield;
        this.energyCost = energyCost;
    }

    public int getYield(String res) {
        return yield.get(res);
    }

    public int getGatherCost() {
        return energyCost;
    }

    @Override
    public String toString() {
        var boosters = getOwner().getPlayer().getObjects().stream()
                .map(obj -> obj.getLoadout(Booster.class).orElse(null))
                .filter(Objects::nonNull)
                .filter(booster -> Grid.objectDistance(booster.getOwner(), getOwner()) <= booster.getBoostRadius())
                .toList();
        var extraResources = new ResourceContainer();
        boosters.forEach(booster -> extraResources.add(booster.getYield()));
        return "Yield: {" + (yield.addAndReturn(extraResources)) + "}";
    }

}
