package core;

import static core.Option.*;

public enum Resource {

    FOOD("Food", HUNT), WATER("Water", DRINK), WOOD("Wood", LOG),
    STONE("Stone", MASON), IRON("Iron", WELD), COAL("Coal", MINE);

    public final String name;
    public final Option operation;

    Resource(String n, Option op) {
        name = n;
        operation = op;
    }

}
