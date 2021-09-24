package core;

import java.awt.*;
import java.util.Map;

public class Gaia extends Player {

    public Gaia() {
        super("Gaia", Color.green, new Color(130, 70, 20));
    }

    public int getResource(Resource type) {
        return 50_000;
    }

    public boolean hasResources(Map<Resource,Integer> res) { return true; }
}
