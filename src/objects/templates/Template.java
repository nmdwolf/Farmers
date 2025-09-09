package objects.templates;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import objects.loadouts.Loadout;
import objects.loadouts.LoadoutFactory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class Template {

    private final HashMap<Class<? extends Loadout>, Loadout> loadouts;

    public int size, sight, health, degradeTime, degradeAmount;
    public String type, sprite;

    public Template() {
        loadouts = new HashMap<>();
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

    @JsonAnySetter
    public void registerExtra(String attr, Object property) {
        if(LoadoutFactory.isRegistered(attr))
            addLoadout(LoadoutFactory.createLoadout(attr, property));
    }
}
