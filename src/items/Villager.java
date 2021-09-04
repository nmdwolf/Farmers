package items;

import core.Contract;
import core.GameConstants;
import core.Player;

import java.util.HashMap;

public class Villager extends ProductiveUnit {

    public final static int VILLAGER_HEALTH = 1000;
    public final static int VILLAGER_ENERGY = 3;
    public final static int VILLAGER_SIZE = 1;

    public Villager(Player p, int x, int y) {
        super(p, new HashMap<>() {{
            put(GameConstants.X_KEY, x);
            put(GameConstants.Y_KEY, y);
            put(GameConstants.HEALTH_KEY, VILLAGER_HEALTH);
            put(GameConstants.STATUS_KEY, GameConstants.ACTIVE);
            put(GameConstants.ENERGY_KEY, VILLAGER_ENERGY);
            put(GameConstants.SIZE_KEY, VILLAGER_SIZE);
            put(GameConstants.VIEW_KEY, p.getViewLevel());
        }});
    }

    @Override
    public String getType() {
        return "Villager";
    }

    @Override
    public String getToken() {
        return "V";
    }
}
