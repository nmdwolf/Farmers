package items.upgrade;

import core.Player;
import core.Resource;
import items.GameObject;

import java.util.Map;

public abstract class Upgrade<T extends GameObject> {

    private final int cycleVisibilityThreshold;
    private final Player player;
    private final Map<Resource, Integer> resources;

    public Upgrade(Player p, Map<Resource, Integer> res, int visibilityThreshold) {
        cycleVisibilityThreshold = visibilityThreshold;
        player = p;
        resources = res;
    }

    public Player getPlayer() { return player; }

    public void upgrade() {
        player.changeResources(resources);
        getPlayer().enableUpgrade(this);
    }

    public abstract void applyTo(T object);

    public boolean isPossible(int cycle) {
        return getPlayer().hasResources(resources);
    }

    public abstract int getID();

    public abstract void notifyObserver(GameObject... objects);

    public boolean isVisible(int cycle) {
        return cycle >= cycleVisibilityThreshold;
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
