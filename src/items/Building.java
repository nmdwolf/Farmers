package items;

import core.GameConstants;
import core.Player;

import java.util.HashMap;

public abstract class Building implements Destroyable {

    private int x, y, viewLevel;
    private int status, maxHealth, health, space, size;
    private int maxFoundation;
    private Player player;

    public Building(Player p, HashMap<Integer, Integer> params) {
        player = p;
        x = params.get(GameConstants.X_KEY);
        y = params.get(GameConstants.Y_KEY);
        viewLevel = params.get(GameConstants.VIEW_KEY);
        maxHealth = params.get(GameConstants.HEALTH_KEY);
        health = params.get(GameConstants.HEALTH_KEY);
        status = params.get(GameConstants.STATUS_KEY);
        space = params.get(GameConstants.SPACE_KEY);
        size = params.get(GameConstants.SIZE_KEY);
    }

    public int getStatus() {
        return status;
    }

    public int getSpace() {
        return space;
    }

    public int getSize() {
        return size;
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
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getViewLevel() {
        return viewLevel;
    }

    @Override
    public void setViewLevel(int level) {
        viewLevel = level;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
