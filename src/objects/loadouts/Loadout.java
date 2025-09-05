package objects.loadouts;

public abstract class Loadout {

    private final String name;

    public Loadout(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
