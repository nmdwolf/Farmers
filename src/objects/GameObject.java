package objects;

import core.*;

import UI.*;
import core.player.Player;
import objects.loadouts.Loadout;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Optional;

public abstract class GameObject {

    private final int id;
    private Cell cell;
    private Player player;
    private int size, sight, health, maxHealth, degradeTime, degradeAmount, startCycle;
    private final HashMap<Class<? extends Loadout>, Loadout> loadouts;

    public GameObject(int size, int sight, int health, int degradeTime,int degradeAmount) {
        id = CustomMethods.getNewIdentifier();

        this.degradeTime = degradeTime;
        this.degradeAmount = degradeAmount;

        this.size = size;
        this.sight = sight;

        this.health = health;
        maxHealth = health;

        loadouts = new HashMap<>();
    }

    public void initialize(Player player, Cell cell, int cycle) {
        setPlayer(player);
        setCell(cell);
        startCycle = cycle;
    }

    public int getObjectIdentifier() { return id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player newPlayer) {
        if(player != null)
            throw new IllegalStateException("Player has already been set.");
        player = newPlayer;
    }

    public Cell getCell() { return cell; }
    public void setCell(Cell cell) {
        if(this.cell != null)
            this.cell.removeContent(this);
        this.cell = cell;
        this.cell.addContent(this);
    }

    public abstract String getClassLabel();
    public abstract String getToken();
    public abstract int getType();

    @NotNull
    public Optional<BufferedImage> getSprite(boolean max) { return Optional.empty(); }

    public int getSize() { return size; }
    public void changeSpace(int amount) { size += amount; }

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

    /**
     * Retrieves the required Loadout of this Unit (if present).
     * @param loadoutClass type of Loadout to be retrieved
     * @return the required Loadout if present
     * @param <T> Class of the required Loadout
     */
    public <T extends Loadout> Optional<T> getLoadout(Class<T> loadoutClass) {
        return Optional.ofNullable(loadoutClass.cast(loadouts.get(loadoutClass)));
    }

    /**
     * Adds a Loadout to this Unit.
     * @param l new Loadout to be added
     */
    public void addLoadout(@NotNull Loadout l) {
        loadouts.put(l.getClass(), l);
    }

    // BASE METHODS INHERITED FROM OBJECT
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof GameObject go) && (id == go.getObjectIdentifier());
    }
}
