package core.upgrade;

import UI.CustomMethods;
import core.player.Player;
import core.resources.ResourceContainer;
import objects.GameObject;

public abstract class InstanceUpgrade<T extends GameObject<T>> extends Upgrade{

    private final T object;
    public final int id;

    public InstanceUpgrade(T obj, ResourceContainer res, int cycleThreshold) {
        super(res, cycleThreshold, "", false);
        object = obj;
        id = CustomMethods.getNewUpgradeIdentifier();
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void apply(GameObject<?> object) {}

    @Override
    public void upgrade(Player p) {
        super.upgrade(p);
        if(!p.equals(getObject().getPlayer()))
            throw new IllegalArgumentException("The specified player should match this instance's owner.");
    }

    public T getObject() { return object; }
}
