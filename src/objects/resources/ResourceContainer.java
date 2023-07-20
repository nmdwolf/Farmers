package objects.resources;

import static objects.resources.Resource.*;

import java.util.HashMap;

public class ResourceContainer extends HashMap<Resource, Integer> {

    public final static ResourceContainer EMPTY_CONTAINER = new ResourceContainer() {{
        put(TIME, 0);
    }};

    public ResourceContainer() {}

    public ResourceContainer(ResourceContainer old) {
        this.putAll(old);
    }

    public ResourceContainer(Resource type, int amount) {
        put(type, amount);
    }

    public ResourceContainer(int food, int water, int wood, int stone, int iron, int coal) {
        put(FOOD, food);
        put(WATER, water);
        put(WOOD, wood);
        put(STONE, stone);
        put(IRON, iron);
        put(COAL, coal);
    }

    public ResourceContainer change(Resource resource, int amount) {
        ResourceContainer resources = new ResourceContainer();
        if(containsKey(resource))
            resources.put(resource, get(resource) + amount);
        else
            resources.put(resource, amount);
        return resources;
    }

    public ResourceContainer add(ResourceContainer extra) {
        ResourceContainer resources = new ResourceContainer();
        for(Resource res : keySet())
            resources.put(res, get(res));
        for(Resource res : extra.keySet()) {
            if (containsKey(res))
                resources.put(res, resources.get(res) + extra.get(res));
            else
                resources.put(res, extra.get(res));
        }
        return resources;
    }

    public ResourceContainer negative() {
        ResourceContainer resources = new ResourceContainer();
        for(Resource res : keySet())
            resources.put(res, -get(res));
        return resources;
    }

    @Override
    public Integer get(Object key) {
        if(containsKey(key))
            return super.get(key);
        else if(key instanceof Resource)
            return 0;
        else
            return null;
    }
}
