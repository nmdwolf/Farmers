package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import objects.GameObject;

public abstract class Loadout {

    private final String type;

    // owner of the loadout (cannot be instantiated by Jackson)
    @JsonIgnore
    private GameObject owner;

    public Loadout(String type) {
        this.type = type;
    }

    /**
     * String representation of this loadout type.
     * @return loadout type
     */
    public String getType() {
        return type;
    }

    /**
     * Changes the owner of this loadout. (E.g. useful when loadouts could be transferred to other objects.)
     * @param owner new owner
     */
    public void setOwner(GameObject owner) {
        this.owner = owner;
    }

    /**
     * Gets the current owner of this loadout.
     * @return current owner
     */
    public GameObject getOwner() {
        return owner;
    }
}
