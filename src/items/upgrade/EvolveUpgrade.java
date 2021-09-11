package items.upgrade;

import core.Options;
import core.ResourceContainer;
import general.CustomMethods;
import items.GameObject;

public class EvolveUpgrade<T extends GameObject> extends InstanceUpgrade<T> {

    public final static int EVOLVE_ID = CustomMethods.getNewUpgradeIdentifier();

    public EvolveUpgrade(T obj, ResourceContainer res, int visibilityThreshold) {
        super(obj, res, visibilityThreshold);
    }

    @Override
    public void applyTo(GameObject object) {
        object.changeValue(Options.LEVEL_KEY, 1);
    }

    @Override
    public int getID() {
        return EVOLVE_ID;
    }

    @Override
    public String toString() {
        return "Evolve";
    }
}
