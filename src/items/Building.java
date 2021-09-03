package items;

import java.util.HashMap;

public abstract class Building implements Destroyable {

    public final static int X_KEY = 0;
    public final static int Y_KEY = 1;
    public final static int HEALTH_KEY = 2;
    public final static int STATUS_KEY = 3;

    public final static int FOUNDATION = 1000;
    public final static int CONSTRUCTION = 1001;
    public final static int BUILT = 1002;

    private int status, maxHealth, health, x, y;
    private int maxFoundation;
    private Player player;

    public Building(Player p, HashMap<Integer, Integer> params) {
        player = p;
        x = params.get(X_KEY);
        y = params.get(Y_KEY);
        maxHealth = params.get(HEALTH_KEY);
        health = params.get(HEALTH_KEY);
        status = params.get(STATUS_KEY);
    }

    public int getStatus() {
        return status;
    }

    @Override
    public int getMaxHealth() {
        return maxHealth;
    }

    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public void changeHealth(int amount) {
        health += amount;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
