package core.player;

import core.Cell;
import core.resources.Resource;

import java.awt.*;
import java.util.Map;

public class Gaia extends Player {

    public Gaia(Cell origin) {
        super("Gaia", Color.green, new Color(130, 70, 20), origin);
    }

    public int getResource(Resource type) {
        return 50_000;
    }

    public boolean hasResources(Map<Resource,Integer> res) { return true; }
}
