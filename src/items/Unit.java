package items;

import core.GameConstants;
import core.Player;

import java.util.HashMap;

public abstract class Unit implements Destroyable, Movable {

    private int x, y, viewLevel;
    private int status, size, maxHealth, health, maxEnergy, energy;
    private Player player;

    public Unit(Player p, HashMap<Integer, Integer> params) {
        player = p;
        x = params.get(GameConstants.X_KEY);
        y = params.get(GameConstants.Y_KEY);
        viewLevel = params.get(GameConstants.VIEW_KEY);
        maxHealth = params.get(GameConstants.HEALTH_KEY);
        health = params.get(GameConstants.HEALTH_KEY);
        status = params.get(GameConstants.STATUS_KEY);
        maxEnergy = params.get(GameConstants.ENERGY_KEY);
        energy = params.get(GameConstants.ENERGY_KEY);
        size = params.get(GameConstants.SIZE_KEY);
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
    public int getEnergy() {
        return energy;
    }

    @Override
    public void changeEnergy(int amount) {
        energy += amount;
    }

    public int getSize() { return size; }

    @Override
    public void reset() {
        energy = maxEnergy;
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

    @Override
    public String toString() {
        return "Type: " + getType() + "\ncore.Player: " + player.getName() +
                "\nHealth: " + health + "/" + maxHealth +
                "\nEnergy: " + energy + "/" + maxEnergy;
    }
}
