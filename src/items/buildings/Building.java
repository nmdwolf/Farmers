package items.buildings;

import static core.Options.*;

import core.*;

import general.CustomMethods;
import items.Constructable;
import items.Destroyable;
import items.GameObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class Building implements Destroyable, Constructable {

    private int maxHealth, health, degradationCycle, degradationAmount;
    private int status, size, cycle, space, maxFoundation, sight;
    private Player player;
    private final int id;
    private final Location location;
    private final Map<Resource, Integer> resources;
    private final HashSet<Integer> descriptions;

    public Building(Player p, Location loc, Map<Resource, Integer> res, Map<Options, Integer> params) {
        id = CustomMethods.getNewIdentifier();

        player = p;
        location = loc;

        maxHealth = params.get(HEALTH_KEY);
        health = params.get(HEALTH_KEY);
        status = params.get(STATUS_KEY);
        space = params.get(SPACE_KEY);
        sight = params.get(SIGHT_KEY);
        size = params.get(SIZE_KEY);
        degradationAmount = params.get(DEGRADATION_AMOUNT_KEY);
        degradationCycle = params.get(DEGRADATION_CYCLE_KEY);
        resources = res;

        descriptions = new HashSet<>();
        descriptions.add(GameConstants.BUILDING_TYPE);
        descriptions.add(GameConstants.DESTROYABLE_TYPE);
        descriptions.add(GameConstants.CONSTRUCTABLE_TYPE);
    }

    public int getStatus() {
        return status;
    }

    public int getSpace() {
        return space;
    }

    @Override
    public int getSize() {
        return size;
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
    public Location getLocation() {
        return location;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void cycle(int cycle) {
        cycle++;
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
                "\nHealth: " + health + "/" + maxHealth;
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
