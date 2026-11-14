package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.util.HashMap;

public class LoadoutFactory {

    // Map of all registered loadouts (or their factory instances)
    private final static HashMap<String, LoadoutCreator<? extends Loadout>> creators = new HashMap<>();

    /**
     * Creates an instance of the given loadout type with the provided specifications (e.g. health and damage
     * reductions).
     * @param type loadout type
     * @param value loadout parameters
     * @return loadout
     */
    public static Loadout createLoadout(String type, Object value) {
        if(value instanceof HashMap)
            return creators.get(type).create(value);
        else {
            return creators.get(type).create(new HashMap<>() {{
                put(type, value);
            }}); // This is a shortcut for single argument loadouts
        }
    }

    /**
     * Checks whether the given loadout type has already been loaded from file.
     * @param type loadout type
     * @return registration status
     */
    public static boolean isRegistered(String type) {
        return creators.containsKey(type);
    }

    /**
     * Registers a new loadout type.
     * @param loadOutClass class corresponding to the new loadout type
     * @param <T> Generic type corresponding to the new loadout type.
     */
    public static <T extends Loadout> void registerLoadout(Class<T> loadOutClass) {
        // Add new class to the map of loadouts using its own label
        creators.put(loadOutClass.getSimpleName().toLowerCase(), map -> {

            // Jackson mapper to turn properties into a class instance
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
            return mapper.convertValue(map, loadOutClass);
        });
    }

    /**
     * Functional interface acting as a Loadout factory.
     * @param <T> Generic type of the required loadout.
     */
    @FunctionalInterface
    public interface LoadoutCreator<T extends Loadout> {
        T create(Object map);
    }
}
