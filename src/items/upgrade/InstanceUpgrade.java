package items.upgrade;

import general.CustomMethods;
import general.ResourceContainer;
import items.GameObject;

public abstract class InstanceUpgrade<T extends GameObject> extends Upgrade<T>{

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
    public void upgrade() {
        super.upgrade();
        applyTo(object);
    }

    @Override
    public void notifyObserver(GameObject... objects) {}
}
