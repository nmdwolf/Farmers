package objects;

import core.player.Award;
import core.Cell;
import core.player.Player;
import core.resources.ResourceContainer;

import java.util.Optional;

import static core.resources.Resource.TIME;

public abstract class Construction extends GameObject {

    private int completed;
    private final int energyCost, buildingTime;
    private final boolean hasVisibleFoundation;
    private final ResourceContainer cost;

    public Construction(Player player, Cell cell, int cycle, int space, int sight, int health,
                        int degradeTime, int degradeAmount, ResourceContainer cost,
                        int energyCost, boolean hasVisibleFoundation) {
        super(player, cell, cycle, space, sight, health, degradeTime, degradeAmount);

        this.cost = cost;

        completed = 0;
        buildingTime = cost.get(TIME);

        this.energyCost = energyCost;
        this.hasVisibleFoundation = hasVisibleFoundation;
    }

    public int getCompletion() {
        return completed;
    }
    public int getRequirement() { return buildingTime; }
    public boolean isCompleted() { return completed >= buildingTime; }

    public void construct() { completed++; }

    public int getEnergyCost() {
        return energyCost;
    }

    public ResourceContainer getCost() { return cost; }

    public boolean hasVisibleFoundation() { return hasVisibleFoundation; }

    public Optional<Award> getConstructionAward() { return Optional.empty(); }
}
