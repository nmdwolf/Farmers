package objects;

import objects.resources.Resource;

public interface Booster {

    /**
     * The number of cells (l1-distance) over which this booster has influence.
     * @return boost radius (in l1-distance)
     */
    int getBoostRadius();

    int getBoostAmount(GameObject obj, Resource res);

}
