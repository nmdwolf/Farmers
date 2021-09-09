package items.units;

import static core.Options.*;

import core.*;
import general.CustomMethods;
import items.Constructable;
import items.Destroyable;
import items.GameObject;
import items.Movable;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class Unit implements Destroyable, Movable, Constructable {

    private int status, maxEnergy, energy, size, sight;
    private int maxHealth, health, degradationCycle, degradationAmount;
    private Player player;
    private Location location;
    private final int id;
    private final Map<Resource, Integer> resources;
    private final HashSet<Integer> descriptions;

    public Unit(Player p, Location loc, Map<Resource, Integer> res, Map<Options, Integer> params) {
        id = CustomMethods.getNewIdentifier();

        player = p;
        location = loc;

        sight = params.get(SIGHT_KEY);
        size = params.get(SIZE_KEY);
        health = maxHealth = params.get(HEALTH_KEY);
        energy = maxEnergy = params.get(ENERGY_KEY);
        status = params.get(STATUS_KEY);
        degradationAmount = params.get(DEGRADATION_AMOUNT_KEY);
        degradationCycle = params.get(DEGRADATION_CYCLE_KEY);

        resources = res;

        descriptions = new HashSet<>();
        descriptions.add(GameConstants.UNIT_TYPE);
        descriptions.add(GameConstants.DESTROYABLE_TYPE);
        descriptions.add(GameConstants.CONSTRUCTABLE_TYPE);
        descriptions.add(GameConstants.MOVABLE_TYPE);
        descriptions.add(GameConstants.DEPLETABLE_TYPE);
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
    public void changeMaxHealth(int amount) {
        maxHealth += amount;
    }

    @Override
    public int getEnergy() {
        return energy;
    }

    @Override
    public void changeEnergy(int amount) {
        energy += amount;
    }

    @Override
    public int getMaxEnergy() {
        return maxEnergy;
    }

    @Override
    public void changeMaxEnergy(int amount) {
        maxEnergy += amount;
    }

    @Override
    public int getSize() { return size; }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setLocation(Location loc) {
        location = loc;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Map<Resource, Integer> getCost() {
        return resources;
    }

    @Override
    public void construct() {
        getPlayer().changeResources(resources);
        getPlayer().addObject(this);
    }

    @Override
    public boolean canConstruct() {
        return player.hasResources(resources);
    }

    @Override
    public void cycle(int cycle) {
        cycle++;
        energy = maxEnergy;
        if(cycle >= degradationCycle)
            degrade();
    }

    @Override
    public void degrade() {
        health -= degradationAmount;
    }

    @Override
    public int getDegradationStart() {
        return degradationCycle;
    }

    @Override
    public void changeDegradationStart(int amount) {
        degradationCycle += amount;
    }

    @Override
    public void destroy() {
        player.removeObject(this);
    }

    @Override
    public GameObject getObject(int description) {
        return this;
    }

    @Override
    public void updateDescriptions(int... desc) {
        for(int description : desc)
            descriptions.add(description);
    }

    @Override
    public Set<Integer> getDescriptions() {
        return descriptions;
    }

    @Override
    public int getLineOfSight() {
        return sight;
    }

    @Override
    public void changeLineOfSight(int amount) {
        sight += amount;
    }

    @Override
    public int getObjectIdentifier() {
        return id;
    }

    @Override
    public String toString() {
        return "Type: " + getType() + "\ncore.Player: " + player.getName() +
                "\nHealth: " + health + "/" + maxHealth +
                "\nEnergy: " + energy + "/" + maxEnergy;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GameObject) {
            return id == ((GameObject) obj).getObjectIdentifier();
        }
        return false;
    }
}
