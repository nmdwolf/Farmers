package objects;

import core.*;

import UI.*;

import java.awt.image.BufferedImage;

public abstract class GameObject {

    private final int id;
    private Cell cell;
    private Player player;
    private int space, sight, health, maxHealth, level, degradeTime, degradeAmount;

    private final int startCycle;

    public GameObject(Player player, Cell cell, int cycle, int space, int sight, int health, int degradeTime, int degradeAmount) {
        id = CustomMethods.getNewIdentifier();

        this.cell = cell;

        this.player = player;
        this.startCycle = cycle;

        this.degradeTime = degradeTime;
        this.degradeAmount = degradeAmount;

        this.space = space;
        this.sight = sight;
        this.level = 1;

        this.health = health;
        maxHealth = health;
    }

    public int getObjectIdentifier() { return id; }

    public Player getPlayer() { return player; }

    public Cell getCell() { return cell; }
    public void setCell(Cell cell) {
        this.cell.removeContent(this);
        this.cell = cell;
        this.cell.addContent(this);
    }

    public abstract String getClassLabel();
    public abstract String getToken();
    public abstract BufferedImage getSprite(boolean max);

    public abstract OperationsList getOperations(int cycle, OperationCode code);

    public int getSpace() { return space; }
    public void changeSpace(int amount) { space += amount; }

    public int getHealth() { return health; }
    public void changeHealth(int amount) { health += amount; }
    public int getMaxHealth() { return maxHealth;}
    public void changeMaxHealth(int amount) {
        health += amount;
        maxHealth += amount;
    }

    public int getSight() { return sight; }
    public void changeSight(int amount) { sight += amount; }

    public void cycle(int cycle) { degrade(cycle); }
    public int getStartCycle() { return startCycle; }

    public void degrade(int cycle) {
        if(degradeTime > 0 && (cycle - getStartCycle()) % degradeTime == 0)
            changeHealth(-degradeAmount);
    }
    public int getDegradeTime() { return degradeTime; }
    public int getDegradeAmount() { return degradeAmount; }

    // BASE METHODS INHERITED FROM OBJECT
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof GameObject) && (id == ((GameObject)obj).getObjectIdentifier());
    }
}
