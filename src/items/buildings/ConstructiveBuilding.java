package items.buildings;

import core.*;
import items.Constructor;

import java.util.Map;

public abstract class ConstructiveBuilding extends Building implements Constructor {

    private Location constructionLocation;

    public ConstructiveBuilding(Player p, Location loc, Map<Resource, Integer> res, Map<Options, Integer> params) {
        super(p, loc, res, params);
        updateDescriptions(GameConstants.WORKER_TYPE);
        updateDescriptions(GameConstants.CONSTRUCTOR_TYPE);

        constructionLocation = new Location(loc.x + 1, loc.y, loc.z);
    }

    @Override
    public Location getConstructionLocation() {
        return constructionLocation;
    }

    @Override
    public void setConstructionLocation(Location loc) {
        constructionLocation = loc;
    }
}
