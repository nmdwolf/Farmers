package items;

import core.*;

import general.*;

import java.awt.image.BufferedImage;

import static core.Status.DEAD;
import static core.Status.IDLE;

public abstract class GameObject {

    private final int id;
    private Cell cell;
    private Player player;
    private int space, sight, health, maxHealth, level, degradeTime, degradeAmount;

    private Status status, oldStatus;

    private final int startCycle;

    public GameObject(Player player, Cell cell, int cycle, int space, int sight, int health, int degradeTime, int degradeAmount) {
        id = CustomMethods.getNewIdentifier();

        this.cell = cell;
        this.cell.addContent(this);

        this.player = player;
        this.startCycle = cycle;

        this.degradeTime = degradeTime;
        this.degradeAmount = degradeAmount;

        this.space = space;
        this.sight = sight;
        this.level = 1;

        this.health = health;
        maxHealth = health;

        this.status = IDLE;
        this.oldStatus = IDLE;
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
    public abstract BufferedImage getSprite();

    public abstract OperationsList getOperations(int cycle);

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


    public abstract void cycle(int cycle);

    public int getStartCycle() { return startCycle; }

    /**
     * Returns the current status of the GameObject.
     * @return current status
     */
    public Status getStatus() { return status; }

    /**
     * Returns the old status of the GameObject.
     * @return old status
     */
    public Status getOldStatus() { return oldStatus; }

    /**
     * Changes the current status of the GameObject.
     * @param newStatus new status
     */
    public void setStatus(Status newStatus) {
        oldStatus = status;
        status = newStatus;
    }

    public void degrade(int cycle) {
        if((cycle - getStartCycle()) % degradeTime == 0)
            changeHealth(-degradeAmount);

        if(health <= 0)
            status = DEAD;
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
