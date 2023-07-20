package core.upgrade;

import core.Player;
import objects.resources.ResourceContainer;
import objects.GameObject;

public abstract class Upgrade {

    private final int cycleVisibilityThreshold;
    private final ResourceContainer resources;
    private final Player player;

    public Upgrade(Player player, ResourceContainer res, int cycleThreshold) {
        cycleVisibilityThreshold = cycleThreshold;
        this.player = player;
        resources = res;
    }

    public boolean isPossible() {
        return player.hasResources(resources);
    }

    public abstract int getID();

    public void upgrade() {
        player.changeResources(resources);
        player.enableUpgrade(this);
        for(GameObject obj : getPlayer().getObjects())
            apply(obj);
    }

    /**
     * Method used to apply core.upgrade post-hoc
     * @param object upgradable object
     */
    public abstract void apply(GameObject object);

    public boolean isVisible() {
        return player.getCycle() >= cycleVisibilityThreshold && !player.hasUpgrade(this);
    }

    public Player getPlayer() { return player; }

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
