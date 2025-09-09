package objects.templates;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class TemplateFactory {

    private static final HashMap<String, ObjectTemplate> templates = new HashMap<>();

    public static <T extends ObjectTemplate> void loadTemplates(Class<T> templateClass) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        File file = new File("src/data/" + templateClass.getSimpleName().replace("Template", "") + ".json");
        try {
            T[] objects = mapper.readValue(file, mapper.getTypeFactory().constructArrayType(templateClass));
            for(T obj : objects)
                templates.put(obj.type, obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ObjectTemplate getTemplate(String type) throws IllegalArgumentException {
        if(templates.containsKey(type))
            return templates.get(type);
        else
            throw new IllegalArgumentException("Template type " + type + " has not been registered.");
    }

    public static boolean isRegistered(String type) {
        return templates.containsKey(type);
    }

}
