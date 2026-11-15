package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Spacer extends Loadout {

    private int space;

    @JsonCreator
    public Spacer(@JsonProperty(required = true, value = "spacer") int space) {
        super("spacer");
        this.space = space;
    }

    public int getSpaceBoost() {
        return space;
    }

    public void increaseSpaceBoost(int amount) {
        space += amount;
    }

    @Override
    public String toString() {
        return "Space: " + space;
    }
}
