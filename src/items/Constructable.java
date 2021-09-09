package items;

import core.Resource;

import java.util.Map;

public interface Constructable extends GameObject{

    Map<Resource, Integer> getCost();

    boolean canConstruct();

    void construct();

    boolean isVisible(int cycle);
}
