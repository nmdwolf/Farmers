package objects.loadouts;

public abstract class Loadout {

    private final String type;

    public Loadout(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
