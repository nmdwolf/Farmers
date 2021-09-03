package items;

import java.util.HashMap;

public class Villager extends Unit {

    public final static int VILLAGER_HEALTH = 1000;
    public final static int VILLAGER_ENERGY = 3;

    public Villager(Player p, int x, int y) {
        super(p, new HashMap<>() {{
            put(X_KEY, x);
            put(Y_KEY, y);
            put(HEALTH_KEY, VILLAGER_HEALTH);
            put(STATUS_KEY, ACTIVE);
            put(ENERGY_KEY, VILLAGER_ENERGY);
        }});
    }

    @Override
    public String toString() {
        return "Type: Villager\nPlayer: " + getPlayer().getName() + "\nHealth: " + getHealth() + "/" + getMaxHealth();
    }

    @Override
    public String getToken() {
        return "V";
    }
}
