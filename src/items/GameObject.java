package items;

import core.*;

import general.CustomMethods;
import items.upgrade.EvolveUpgrade;
import items.upgrade.Upgrade;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public abstract class GameObject {

    private final int id;
    private final HashSet<Type> descriptions;

    private int cycle, sight, size;

    protected Location location;
    protected Player player;

    public GameObject(Player player, Location location, int size, int sight) {
        id = CustomMethods.getNewIdentifier();

        descriptions = new HashSet<>();

        this.location = location;
        this.player = player;
        this.sight = sight;
        this.size = size;
    }

    public Location getLocation() { return location; }
    public void setLocation(Location loc) { location = loc;};

    public Player getPlayer() { return player; }

    public int getObjectIdentifier() { return id; }

    public Set<Type> getDescriptions() { return descriptions; }
    public void updateDescriptions(Type... descr) {
        descriptions.addAll(Arrays.asList(descr));
    }

    public GameObject getObject(Type description) { return this; }

    public abstract String getType();
    public abstract String getToken();

    public abstract List<Upgrade> getUpgrades();
    public abstract List<GameObject> getProducts();
    public abstract List<EvolveUpgrade> getEvolutions();

    // Umbrella methods

    public ResourceContainer getResources(Options option) { return ResourceContainer.EMPTY_CONTAINER; }
    public abstract boolean checkStatus(Options option);
    public void perform(Options option) {
        if(option == Options.CYCLE_KEY)
            cycle++;
    }
    public int getValue(Options option) {
        return switch(option) {
            case CYCLE_KEY: yield cycle;
            case SIGHT_KEY: yield sight;
            case SIZE_KEY: yield size;
            default: yield -1;
        };
    }
    public void changeValue(Options option, int amount) {
        if(option == Options.SIGHT_KEY)
            sight += amount;
    }

    public void typedDo(Type type, TypedConsumer toRun) throws TypeException {
        if(getDescriptions().contains(type))
            toRun.accept(this.getObject(type));
    }

    // BASE METHODS

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
