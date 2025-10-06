package core.player;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Memory {

    @JsonProperty
    private int exp;

    @JsonProperty
    private String name;

    public Memory() {}

    public Memory(String name) {
        this.exp = 0;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Adds the specified {@code Player}'s experience to this memory and saves it.
     * @param p player to save
     * @throws IOException
     */
    public void output(Player p) throws IOException {
        for(String res : p.getGained().keySet())
            exp += p.getGained(res);

        File dir = new File("./SavedGames/");
        if(!dir.exists())
            dir.mkdir();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File("./SavedGames/" + name + ".json"), this);
    }

    public static Memory load(String name) throws IOException {
        File dir = new File("./SavedGames/");
        if(dir.exists()) {
            for(File file : dir.listFiles()) {
                if(file.getName().equals(name + ".json")) {
                    ObjectMapper mapper
                            = new ObjectMapper();
                    mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
                    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
                    return mapper.readValue(file, Memory.class);
                }
            }
        }

        return new Memory(name);
    }
}
