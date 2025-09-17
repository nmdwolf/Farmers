package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import objects.GameObject;

public abstract class Loadout {

    private final String type;

    @JsonIgnore
    private GameObject<?> owner;

    public Loadout(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setOwner(GameObject<?> owner) {
        this.owner = owner;
    }

    public GameObject<?> getOwner() {
        return owner;
    }
}
