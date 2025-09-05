package core.player;

import core.Cell;

import java.awt.*;
import java.util.Map;

public class Gaia extends Player {

    public Gaia(Cell origin) {
        super("Gaia", Color.green, new Color(130, 70, 20), origin);
    }

    public int getResource(String type) {
        return 50_000;
    }

    public boolean hasResources(Map<String,Integer> res) { return true; }
}
