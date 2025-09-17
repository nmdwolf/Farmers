package objects.loadouts;

import UI.CustomMethods;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.resources.ResourceContainer;

import java.util.Objects;

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
    public String toString() {
        var boosters = getOwner().getPlayer().getObjects().stream()
                .map(obj -> obj.getLoadout(Booster.class).orElse(null))
                .filter(Objects::nonNull)
                .filter(booster -> CustomMethods.objectDistance(booster.getOwner(), getOwner()) <= booster.getBoostRadius())
                .toList();
        var extraResources = new ResourceContainer();
        boosters.forEach(booster -> extraResources.add(booster.getYield()));
        return "Yield: {" + (yield.addAndReturn(extraResources)) + "}";
    }

}
