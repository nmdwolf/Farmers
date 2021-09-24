package items.buildings;

import core.*;
import general.ResourceContainer;

import java.util.Map;

public abstract class ConstructiveBuilding extends Building {

    public ConstructiveBuilding(Player p, Location loc, ResourceContainer cost, Map<Option, Integer> params) {
        super(p, loc, cost, params);
        updateTypes(Type.CONSTRUCTOR);

        setValue(Option.CONSTRUCT_X, 1);
        setValue(Option.CONSTRUCT_Y, 0);
    }
}
