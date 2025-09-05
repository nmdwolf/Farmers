package core.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.List;

public class ResourceContainer extends HashMap<Resource, Integer> {

    public final static ResourceContainer EMPTY_CONTAINER = new ResourceContainer();

    public ResourceContainer() {}

    public ResourceContainer(ResourceContainer old) {
        putAll(old);
    }

    public ResourceContainer(Resource type, int amount) {
        put(type, amount);
    }

    @JsonCreator
    public ResourceContainer(@JsonProperty("resources") Resource[] resources, @JsonProperty("amounts") int[] amounts) {
        if(resources.length != amounts.length)
            throw new IllegalArgumentException("Both arguments should be of equal size: " + resources.length + " vs. " + amounts.length);

        for(int i = 0; i < resources.length; i++)
            put(resources[i], amounts[i]);
    }

    public ResourceContainer(List<Resource> resources, List<Integer> amounts) {
        if(resources.size() != amounts.size())
            throw new IllegalArgumentException("Both arguments should be of equal size: " + resources.size() + " vs. " + amounts.size());

        for(int i = 0; i < resources.size(); i++)
            put(resources.get(i), amounts.get(i));
    }

    public ResourceContainer(HashMap<Resource, Integer> amounts) {
        putAll(amounts);
    }

    /**
     * Adds the specified amount of the given {@code Resource} to the current resource container (this is performed in place).
     * @param resource resource type
     * @param amount amount to add (or subtract if negative)
     */
    public void add(Resource resource, int amount) {
        if(containsKey(resource))
            put(resource, get(resource) + amount);
        else
            put(resource, amount);
    }

    public ResourceContainer negative() {
        ResourceContainer resources = new ResourceContainer();
        for(Resource res : keySet())
            resources.put(res, -get(res));
        return resources;
    }

    @Override
    public Integer get(Object key) {
        if(!(key instanceof Resource res))
            throw new IllegalArgumentException("The provided key is not a valid Resource.");
        else
            return getOrDefault(res, 0);
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for(Resource res : keySet())
            output.append(res.getName()).append(": ").append(get(res)).append(", ");
        return output.toString();
    }
}
