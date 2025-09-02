package core.upgrade;

import UI.CustomMethods;
import core.resources.ResourceContainer;
import objects.GameObject;

public abstract class InstanceUpgrade<T extends GameObject> extends Upgrade{

    private final T object;
    public final int id;

    public InstanceUpgrade(T obj, ResourceContainer res, int cycleThreshold) {
        super(obj.getPlayer(), res, cycleThreshold);
        object = obj;
        id = CustomMethods.getNewUpgradeIdentifier();
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void apply(GameObject object) {}

    public T getObject() { return object; }
}
