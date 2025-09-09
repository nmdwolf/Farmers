package objects.loadouts;

import objects.templates.Template;

public abstract class Loadout<T extends Template> {

    private final String type;

    public Loadout(T temp) {
        this.type = temp.type;
    }

    public String getType() {
        return type;
    }

}
