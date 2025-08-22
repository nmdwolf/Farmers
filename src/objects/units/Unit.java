package objects.units;

import core.*;
import core.player.Player;
import objects.Operational;
import objects.resources.ResourceContainer;

import static core.GameConstants.COLD_LEVEL;

public abstract class Unit extends Operational {

    public final static int UNIT_DIFFICULTY = 1;

    private int energy, maxEnergy;

    private final int animationDelay;

    public Unit(Player p, Cell cell, int cycle, int animationDelay, int size, int sight, int health,
                int degradeTime, int degradeAmount, int cycleLength,
                int energy, ResourceContainer cost) {
        super(p, cell, cycle, size, sight, health, degradeTime, degradeAmount, cycleLength,
                cost, UNIT_DIFFICULTY, false);

        this.animationDelay = animationDelay;
        this.energy = energy;
        maxEnergy = energy;
    }

    @Override
    public void cycle(int cycle) {
        super.cycle(cycle);
        energy = maxEnergy;
        changeHealth(Math.min(0, getCell().getHeatLevel() - COLD_LEVEL));
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
