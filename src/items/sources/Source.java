package items.sources;

import core.Resource;
import items.GameObject;

import java.util.Map;

public interface Source extends GameObject {

    Map<Resource, Integer> getResources();

}
