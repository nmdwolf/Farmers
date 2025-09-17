package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Spacer extends Loadout implements objects.Spacer {

    private int spacer;

    @JsonCreator
    public Spacer(int spacer) {
        super("spacer");
        this.spacer = spacer;
    }

    @Override
    public int getSpaceBoost() {
        return spacer;
    }

    @Override
    public void changeSpaceBoost(int amount) {
        spacer += amount;
    }

    @Override
    public String toString() {
        return "Space: " + spacer;
    }
}
