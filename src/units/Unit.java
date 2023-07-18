package units;

import core.*;
import resources.ResourceContainer;
import items.Constructable;

public abstract class Unit extends Constructable {

    public final static int UNIT_DIFFICULTY = 1;

    private int energy, maxEnergy;

    private final int animationDelay;

    public Unit(Player p, Cell cell, int cycle, int animationDelay, int size, int sight, int health,
                int degradeTime, int degradeAmount,
                int energy, ResourceContainer cost) {
        super(p, cell, cycle, size, sight, health, degradeTime, degradeAmount,
                cost, UNIT_DIFFICULTY, false);

        this.animationDelay = animationDelay;
        this.energy = energy;
        maxEnergy = energy;
    }

    @Override
    public void cycle(int cycle) {
        energy = maxEnergy;
    }

    public int getEnergy() {
        return energy;
    }

    public void changeEnergy(int amount) {
        energy += amount;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public void changeMaxEnergy(int amount) {
        maxEnergy += amount;
        energy += amount;
    }

    public int getAnimationDelay() { return animationDelay; }

    @Override
    public String toString() {
        return "Type: " + getClassLabel() + "\nPlayer: " + getPlayer().getName() +
                "\nHealth: " + getHealth() + "/" + getMaxHealth() +
                "\nEnergy: " + energy + "/" + maxEnergy;
    }
}
