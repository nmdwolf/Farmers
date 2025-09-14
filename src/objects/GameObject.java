package objects;

import core.*;

import UI.*;
import core.player.Player;
import objects.loadouts.Loadout;
import objects.templates.Template;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Optional;

import static core.GameConstants.SPRITE_SIZE;
import static core.GameConstants.SPRITE_SIZE_MAX;

public abstract class GameObject<G extends GameObject<G>> {

    private final static HashMap<String, Optional<BufferedImage>> sprites = new HashMap<>(), maxiSprites = new HashMap<>();

    private final int id;
    private final HashMap<Class<? extends Loadout>, Loadout> loadouts;
    private Cell cell;
    private Player player;
    private final Template template;

    private int size, sight, health, maxHealth, degradeTime, degradeAmount, startCycle;
    private boolean changeFlag;

    public GameObject(Template temp) {
        id = CustomMethods.getNewIdentifier();
        changeFlag = false;

        this.degradeTime = temp.degradeTime;
        this.degradeAmount = temp.degradeAmount;

        this.size = temp.size;
        this.sight = temp.sight;

        this.health = temp.health;
        maxHealth = temp.health;

        loadouts = new HashMap<>(temp.getLoadouts());
        template = temp;
    }

    public Template getTemplate() {
        return template;
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

    public String getClassLabel() {
        return template.type;
    }
    public String getToken() {
            return template.type.substring(0, 1);
    }
    public abstract int getType();

    @NotNull
    public Optional<BufferedImage> getSprite(boolean max) { return GameObject.getSprite(template.type, max); }

    public int getSize() { return size; }
    public void changeSpace(int amount) { size += amount; }

    public int getHealth() { return health; }
    public void changeHealth(int amount) {
        health = Math.min(health + amount, maxHealth);
    }
    public int getMaxHealth() { return maxHealth;}
    public void changeMaxHealth(int amount) {
        health += amount;
        maxHealth += amount;
    }

    public int getSight() { return sight; }
    public void changeSight(int amount) { sight += amount; }

    /**
     * Starts a new cycle for this object.
     * This method should reset all cycle-based parameters (e.g. Energy for {@code Operational} objects.
     * @param cycle new cycle
     */
    public void cycle(int cycle) { degrade(cycle); }
    public final int getStartCycle() { return startCycle; }

    public void degrade(int cycle) {
        if(cycle != startCycle && degradeTime > 0 && (cycle - startCycle) % degradeTime == 0)
            changeHealth(-degradeAmount);
    }
    public int getDegradeTime() { return degradeTime; }
    public int getDegradeAmount() { return degradeAmount; }

    /**
     * Indicates that something about this GameObject has changed.
     * This will enable the game loop to repaint.
     */
    public void alertChange() {
        changeFlag = true;
    }

    /**
     * Indicates whether something about this GameObject has changed.
     * @return if something has changed
     */
    public boolean hasChanged() {
        boolean temp = changeFlag;
        changeFlag = false;
        return temp;
    }

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
        return (obj instanceof GameObject<?> go) && (id == go.getObjectIdentifier());
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for(Loadout loadout : loadouts.values())
            s.append("\n\n").append(loadout);

        return s.toString();
    }

    public static void registerSprite(String name, String fileName) {
        sprites.put(name, CustomMethods.loadSprite("src/img/" + fileName + ".png", SPRITE_SIZE, SPRITE_SIZE));
        maxiSprites.put(name, CustomMethods.loadSprite("src/img/" + fileName + ".png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX));
    }

    public static Optional<BufferedImage> getSprite(String name, boolean max) {
        if(max)
            return maxiSprites.get(name);
        else
            return sprites.get(name);
    }
}
