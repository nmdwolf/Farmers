package objects;

import core.*;

import UI.*;
import core.player.Player;
import core.upgrade.Upgrade;
import objects.loadouts.Loadout;
import objects.loadouts.LoadoutFactory;
import objects.templates.Template;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import static core.GameConstants.SPRITE_SIZE_MAX;

public abstract class GameObject<G extends GameObject<G>> {

    private final static HashMap<String, Optional<BufferedImage>> sprites = new HashMap<>();
    private final static HashMap<String, String> spriteLocations = new HashMap<>();

    private final int id;
    private final HashMap<String, Loadout> loadouts;
    private final ArrayList<Upgrade> upgrades;
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

        loadouts = new HashMap<>();
        var props = new HashMap<>(temp.getLoadouts());
        for(String type : props.keySet()) {
            Loadout l = LoadoutFactory.createLoadout(type, props.get(type));
            loadouts.put(type, l);
            l.setOwner(this);
        }

        upgrades = temp.getUpgrades();
        template = temp;
    }

    /**
     * Returns the {@code Template} used to create this object.
     * @return creation template
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * Gives a list of possible upgrades that this {@code GameObject} can perform.
     * @return available upgrades
     */
    public ArrayList<Upgrade> getUpgrades() {
        return upgrades.stream().filter(u -> u.isVisible(player)).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Initializes this object after creation. Sets the owner, current location and current cycle.
     * @param player owner
     * @param cell current location
     * @param cycle current cycle
     */
    public void initialize(Player player, Cell cell, int cycle) {
        setPlayer(player);
        setCell(cell);
        startCycle = cycle;
    }

    /**
     * Returns the identifier of this object. This is unique for each object (and used for the implementation of {@code hashCode()} and {@code equals(Object obj)}).
     * @return unique identifier
     */
    public int getObjectIdentifier() { return id; }

    /**
     * Gives the object's current owner.
     * @return owner
     */
    public Player getPlayer() { return player; }

    /**
     * Sets the object's owner.
     * @param newPlayer new owner
     */
    public void setPlayer(Player newPlayer) {
        if(player != null)
            throw new IllegalStateException("Player has already been set.");
        player = newPlayer;
    }

    /**
     * Gives the object's current location.
     * @return location
     */
    public Cell getCell() { return cell; }

    /**
     * Sets the object's location.
     * @param cell new location
     */
    public void setCell(Cell cell) {
        if(this.cell != null)
            this.cell.removeContent(this);
        this.cell = cell;
        this.cell.addContent(this);
    }

    /**
     * Gives the object's class identifier (retrieved from its underlying template).
     * @return class identifier
     */
    public String getClassLabel() {
        return template.type;
    }

    /**
     * Shorthand for its class identifier (used for graphics).
     * @return class identifier shorthand
     */
    public String getToken() {
            return template.type.substring(0, 1);
    }

    /**
     * Gives the object's type (e.g. Unit or Building).
     * @return object type
     */
    public abstract int getType();

    /**
     * Retrieves the sprite corresponding to this object's class identifier (if available).
     * @return sprite (if available)
     */
    @NotNull
    public Optional<BufferedImage> getSprite() { return GameObject.getSprite(template.type); }

    /**
     * Gives the amount of space occupied by this object.
     * @return occupied space
     */
    public int getSize() { return size; }

    /**
     * Sets the amount of space occupied by this object.
     * @param amount space change
     */
    public void changeSpace(int amount) { size += amount; }

    /**
     * Gives the current health of this object.
     * @return health
     */
    public int getHealth() { return health; }

    /**
     * Changes the current health of this object.
     * @param amount health change
     */
    public void changeHealth(int amount) {
        health = Math.min(health + amount, maxHealth);
    }

    /**
     * Gives the health of this object when it is at full health.
     * @return maximum health
     */
    public int getMaxHealth() { return maxHealth;}

    /**
     * Changes the maximum health of this object.
     * @param amount health change
     */
    public void changeMaxHealth(int amount) {
        health += amount;
        maxHealth += amount;
    }

    /**
     * Gives this object's observation distance.
     * @return observation distance
     */
    public int getSight() { return sight; }

    /**
     * Changes this object's observation distance.
     * @param amount distance change
     */
    public void changeSight(int amount) { sight += amount; }

    /**
     * Starts a new cycle for this object.
     * This method should reset all cycle-based parameters (e.g. Energy for {@code Operational} objects.
     * @param cycle new cycle
     */
    public void cycle(int cycle) { degrade(cycle); }

    /**
     * Gives the cycle during which this object was created (set by {@code initialize()}).
     * @return creation cycle
     */
    public final int getStartCycle() { return startCycle; }

    /**
     * Damages this object based on its 'age'.
     * @param cycle current cycle
     */
    public void degrade(int cycle) {
        if(cycle != startCycle && degradeTime > 0 && (cycle - startCycle) % degradeTime == 0)
            changeHealth(-degradeAmount);
    }

    /**
     * Gives the time at which this object will start degrading.
     * @return degradation start
     */
    public int getDegradeTime() { return degradeTime; }

    /**
     * Gives the amount of health this object will lose every cycle beyond its degradation start.
     * @return damage taken by degradation
     */
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
        return Optional.ofNullable(loadoutClass.cast(loadouts.get(loadoutClass.getSimpleName().toLowerCase())));
    }

    /**
     * Adds a Loadout to this Unit.
     * @param l new Loadout to be added
     */
    public void addLoadout(@NotNull Loadout l) {
        loadouts.put(l.getClass().getSimpleName().toLowerCase(), l);
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

    /**
     * Registers a new sprite corresponding to the specified class identifier.
     * @param name class identifier
     * @param fileName sprite location
     */
    public static void registerSprite(String name, String fileName) {
        spriteLocations.put(name, fileName);
        sprites.put(name, CustomMethods.loadSprite("src/img/" + fileName + ".png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX));
    }

    public static void resizeSprites(int size) {
        var names = sprites.keySet();
        for(String name : names)
            sprites.put(name, CustomMethods.loadSprite("src/img/" + spriteLocations.get(name) + ".png", size, size));
    }

    /**
     * Retrieves a sprite corresponding to the specified class identifier (if available).
     * @param name class identifier
     * @return sprite (if available)
     */
    public static Optional<BufferedImage> getSprite(String name) {
        return sprites.get(name);
    }
}
