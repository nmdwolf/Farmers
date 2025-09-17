package objects;

import core.resources.ResourceContainer;

public interface Booster {

    /**
     * The number of cells (l1-distance) over which this booster has influence.
     * @return boost radius (in l1-distance)
     */
    int getBoostRadius();

    // TODO Why also a GameObject?
    int getBoostAmount(GameObject<?> obj, String res);

    ResourceContainer getYield();
}
