package core.upgrade;

import UI.CustomMethods;
import core.player.Player;
import core.resources.ResourceContainer;
import objects.GameObject;

public abstract class Upgrade {

    private final int threshold;
    private final ResourceContainer cost;
    private final int ID;
    private final String description;
    private final boolean global;

    public Upgrade(ResourceContainer cost, int cycleThreshold, String description, boolean global) {
        ID = CustomMethods.getNewUpgradeIdentifier();

        threshold = cycleThreshold;
        this.description = description;
        this.cost = cost;
        this.global = global;
    }

    /**
     * Gives the description of this {@code Upgrade}.
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gives the cost of this {@code Upgrade}.
     * @return cost
     */
    public ResourceContainer getCost() {
        return cost;
    }

    /**
     * Checks whether the owning {@code Player} has sufficient resources to acquire this {@code Upgrade}.
     * @return whether player has sufficient resources
     */
    public boolean isPossible(Player p) {
        return p.hasResources(cost);
    }

    /**
     * Gives the unique identifier of this {@code Upgrade}.
     * @return unique identifier
     */
    public int getID() {
        return ID;
    }

    /**
     * Applies this upgrade to all existing objects of the owner in case this is a global upgrade.
     */
    public void upgrade(Player p) {
        if(global) {
            p.changeResources(cost.negative());
            p.enableUpgrade(this);
            for (GameObject<?> obj : p.getObjects())
                apply(obj);
        }
    }

    /**
     * Method used to apply upgrades post-hoc
     * @param object upgradable object
     */
    public abstract void apply(GameObject<?> object);

    public boolean isVisible(Player p) {
        return p.getCycle() >= threshold && !p.hasUpgrade(this);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Upgrade)
            return ((Upgrade) obj).getID() == getID();
        return false;
    }

    @Override
    public int hashCode() {
        return getID();
    }
}
