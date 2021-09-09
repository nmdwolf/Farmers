package items.units;

import core.*;
import items.Constructor;
import items.Worker;

import java.util.ArrayList;
import java.util.Map;

public abstract class ConstructiveUnit extends WorkingUnit implements Constructor {

    public ConstructiveUnit(Player p, Location loc, Map<Resource, Integer> res, Map<Options, Integer> params) {
        super(p, loc, res, params);
        updateDescriptions(GameConstants.CONSTRUCTOR_TYPE);
    }

    @Override
    public Location getConstructionLocation() {
        return getLocation();
    };

    @Override
    public void setConstructionLocation(Location loc) {};
}
