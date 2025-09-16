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

    private final HashMap<Class<? extends Loadout>, Loadout> loadouts;

    @JsonIgnore
    private final ArrayList<Upgrade> upgrades;

    public int size, sight, health, degradeTime, degradeAmount;
    public String type, sprite;

    public Template() {
        loadouts = new HashMap<>();
        upgrades = new ArrayList<>();
    }

    public HashMap<Class<? extends Loadout>, Loadout> getLoadouts() {
        return loadouts;
    }

    /**
     * Adds a Loadout to this Template.
     * @param l new Loadout to be added
     */
    public void addLoadout(@NotNull Loadout l) {
        loadouts.put(l.getClass(), l);
    }

    @JsonIgnore
    public ArrayList<Upgrade> getUpgrades() {
        return upgrades;
    }

    @JsonAnySetter
    public void registerExtra(String attr, Object property) {
        if(LoadoutFactory.isRegistered(attr))
            addLoadout(LoadoutFactory.createLoadout(attr, property));

        if(attr.equals("upgrades")) {
            var array = (ArrayList<HashMap<String, Object>>) property;
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
            for (var map : array)
                upgrades.add(mapper.convertValue(map, BasicUpgrade.class));
        }
    }
}
