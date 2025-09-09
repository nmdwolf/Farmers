package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import objects.templates.Template;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class LoadoutFactory {

    private final static HashMap<String, LoadoutCreator<? extends Loadout<?>>> creators = new HashMap<>();

    public static Loadout<? extends Template> createLoadout(String type, Object map) {
        return creators.get(type).create(map);
    }

    public static boolean isRegistered(String type) {
        return creators.containsKey(type);
    }

    public static <T extends Template> void registerLoadout(String type, Class<? extends Loadout<T>> loadOutClass, Class<T> templateClass) {
        creators.put(type, map -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
            try {
                return loadOutClass.getConstructor(templateClass).newInstance(mapper.convertValue(map, templateClass));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @FunctionalInterface
    public interface LoadoutCreator<T extends Loadout<?>>{
        T create(Object map);
    }
}
