package items;

import resources.Resource;

public interface Booster {

    public int getBoostRadius();

    public int getBoostAmount(GameObject obj, Resource res);

}
