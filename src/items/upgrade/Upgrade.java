package items.upgrade;

import core.Player;
import general.ResourceContainer;
import items.GameObject;

public abstract class Upgrade<T extends GameObject> {

    private final int cycleVisibilityThreshold;
    private final ResourceContainer resources;
    private final Player player;

    public Upgrade(Player player, ResourceContainer res, int cycleThreshold) {
        cycleVisibilityThreshold = cycleThreshold;
        this.player = player;
        resources = res;
    }

    public abstract void applyTo(T object);

    public boolean isPossible() {
        return player.hasResources(resources);
    }

    public abstract int getID();

    /**
     * Method used to apply upgrade post-hoc
     * @param objects array of possible upgradables
     */
    public abstract void notifyObserver(GameObject... objects);

    public void upgrade() {
        player.changeResources(resources);
        player.enableUpgrade(this);
    }

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
