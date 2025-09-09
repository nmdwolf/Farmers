package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import core.resources.ResourceContainer;
import objects.templates.GathererTemplate;

import java.util.HashMap;

public class Gatherer extends Loadout<GathererTemplate> {

    private ResourceContainer yield;

    private Gatherer(GathererTemplate temp) {
        super(temp);
        this.yield = temp.yield;
    }

    public int getYield(String res) {
        return yield.get(res);
    }

    @Override
    public String toString() {
        return yield.toString();
    }

    public static Gatherer createGatherer(HashMap<String, Object> json) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        return mapper.convertValue(json, Gatherer.class);
    }

}
