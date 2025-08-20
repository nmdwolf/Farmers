package objects;

import core.Award;
import core.Cell;
import core.Player;
import objects.resources.ResourceContainer;

import static objects.resources.Resource.TIME;

public abstract class Constructable extends GameObject implements Evolvable {

    private int completed, level;
    private final int difficulty, required;
    private final boolean hasVisibleFoundation;
    private final ResourceContainer cost;

    public Constructable(Player player, Cell cell, int cycle, int space, int sight, int health,
                         int degradeTime, int degradeAmount, ResourceContainer cost,
                         int difficulty, boolean hasVisibleFoundation) {
        super(player, cell, cycle, space, sight, health, degradeTime, degradeAmount);

        this.cost = cost;
        this.level = 1;

        completed = 0;
        required = cost.get(TIME);

        this.difficulty = difficulty;
        this.hasVisibleFoundation = hasVisibleFoundation;
    }

    public int getCompletion() {
        return completed;
    }
    public int getRequirement() { return required; }
    public boolean isCompleted() { return completed >= required; }

    public void construct() { completed++; }

    public int getDifficulty() {
        return difficulty;
    }

    public ResourceContainer getCost() { return cost; }

    public boolean hasVisibleFoundation() { return hasVisibleFoundation; }

    public abstract Award getConstructionAward();

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void increaseLevel() {
        level++;
    }
}
