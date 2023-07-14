package items.buildings;

import core.*;
import general.ResourceContainer;

import java.util.Map;

public abstract class ConstructiveBuilding extends Building {

    public ConstructiveBuilding(Player p, Cell cell, int size, ResourceContainer cost, int difficulty, Map<Option, Integer> params) {
        super(p, cell, size, cost, difficulty, params);

        setValue(Option.CONSTRUCT_X, 1);
        setValue(Option.CONSTRUCT_Y, 0);
    }
}
