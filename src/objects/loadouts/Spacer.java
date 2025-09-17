package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Spacer extends Loadout implements objects.Spacer {

    private int space;

    @JsonCreator
    public Spacer(@JsonProperty(required = true, value = "spacer") int space) {
        super("spacer");
        this.space = space;
    }

    @Override
    public int getSpaceBoost() {
        return space;
    }

    @Override
    public void changeSpaceBoost(int amount) {
        space += amount;
    }

    @Override
    public String toString() {
        return "Space: " + space;
    }
}
