package items.upgrade;

import core.Player;
import core.Resource;
import items.GameObject;

import java.util.Map;

public abstract class InstanceUpgrade<T extends GameObject> extends Upgrade<T>{

    private final T object;

    public InstanceUpgrade(Player player, Map<Resource, Integer> res, int visibilityThreshold, T obj) {
        super(player, res, visibilityThreshold);
        object = obj;
    }

    @Override
    public void upgrade() {
        super.upgrade();
        applyTo(object);
    }

    @Override
    public void notifyObserver(GameObject... objects) {}
}
