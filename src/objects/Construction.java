package objects;

import core.player.Award;
import core.resources.ResourceContainer;

import java.util.Optional;

public abstract class Construction extends GameObject {

    private int completed;
    private final int energyCost, buildingTime;
    private final boolean hasVisibleFoundation;
    private final ResourceContainer cost;

    public Construction(int space, int sight, int health,
                        int degradeTime, int degradeAmount, ResourceContainer cost,
                        int energyCost, boolean hasVisibleFoundation) {
        super(space, sight, health, degradeTime, degradeAmount);

        this.cost = cost;

        completed = 0;
        buildingTime = cost.get("Time");

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
