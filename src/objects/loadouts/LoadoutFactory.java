package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.util.HashMap;

public class LoadoutFactory {

    private final static HashMap<String, LoadoutCreator<? extends Loadout>> creators = new HashMap<>();

    public static Loadout createLoadout(String type, Object value) {
        if(value instanceof HashMap)
            return creators.get(type).create(value);
        else {
            return creators.get(type).create(new HashMap<>() {{
                put(type, value);
            }}); // This is a shortcut for single argument loadouts
        }
    }

    public static boolean isRegistered(String type) {
        return creators.containsKey(type);
    }

    public static <T extends Loadout> void registerLoadout(Class<T> loadOutClass) {
        creators.put(loadOutClass.getSimpleName().toLowerCase(), map -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
            return mapper.convertValue(map, loadOutClass);
        });
    }

    @FunctionalInterface
    public interface LoadoutCreator<T extends Loadout> {
        T create(Object map);
    }
}
