package core.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ResourceContainer extends HashMap<String, Integer> {

    public final static ResourceContainer EMPTY_CONTAINER = new ResourceContainer();

    public ResourceContainer() {}

    public ResourceContainer(ResourceContainer old) {
        putAll(old);
    }

    public ResourceContainer(String type, int amount) {
        put(type, amount);
    }

    @JsonCreator
    public ResourceContainer(@JsonProperty("resources") String[] resources, @JsonProperty("amounts") int[] amounts) {
        if(resources.length != amounts.length)
            throw new IllegalArgumentException("Both arguments should be of equal size: " + resources.length + " vs. " + amounts.length);

        for(int i = 0; i < resources.length; i++)
            put(resources[i], amounts[i]);
    }

    public ResourceContainer(List<String> resources, List<Integer> amounts) {
        if(resources.size() != amounts.size())
            throw new IllegalArgumentException("Both arguments should be of equal size: " + resources.size() + " vs. " + amounts.size());

        for(int i = 0; i < resources.size(); i++)
            put(resources.get(i), amounts.get(i));
    }

    public ResourceContainer(HashMap<String, Integer> amounts) {
        putAll(amounts);
    }

    /**
     * Adds the specified amount of the given {@code Resource} to the current resource container (this is performed in place).
     * @param resource resource type
     * @param amount amount to add (or subtract if negative)
     */
    public void add(String resource, int amount) {
        if(containsKey(resource))
            put(resource, get(resource) + amount);
        else
            put(resource, amount);
    }

    public ResourceContainer negative() {
        ResourceContainer resources = new ResourceContainer();
        for(String res : keySet())
            resources.put(res, -get(res));
        return resources;
    }

    @Override
    public Integer get(Object key) {
        if(!(key instanceof String res))
            throw new IllegalArgumentException("The provided key is not a valid Resource.");
        else
            return getOrDefault(res, 0);
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for(String res : keySet())
            output.append(res).append(": ").append(get(res)).append(", ");
        return output.toString();
    }
}
