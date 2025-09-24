package objects.templates;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import core.upgrade.BasicUpgrade;
import core.upgrade.Upgrade;
import objects.loadouts.Loadout;
import objects.loadouts.LoadoutFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class Template {

    private final HashMap<String, Object> loadouts;

    @JsonIgnore
    private final ArrayList<Upgrade> upgrades;

    public int size, sight, health, degradeTime, degradeAmount;
    public String type, sprite;
    public String[] tags = new String[]{};

    public Template() {
        loadouts = new HashMap<>();
        upgrades = new ArrayList<>();
    }

    public HashMap<String, Object> getLoadouts() {
        return loadouts;
    }

    /**
     * Adds a Loadout to this Template.
     * @param properties Loadout properties to be added
     */
    public void addLoadout(String type, @NotNull Object properties) {
        loadouts.put(type, properties);
    }

    @JsonIgnore
    public ArrayList<Upgrade> getUpgrades() {
        return upgrades;
    }

    @JsonAnySetter
    public void registerExtra(String attr, Object properties) {
        if(LoadoutFactory.isRegistered(attr))
            addLoadout(attr, properties);

        if(attr.equals("upgrades")) {
            var array = (ArrayList<HashMap<String, Object>>) properties;
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
            for (var map : array)
                upgrades.add(mapper.convertValue(map, BasicUpgrade.class));
        }
    }
}
