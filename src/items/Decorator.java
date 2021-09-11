package items;

import core.Location;
import core.Options;
import core.ResourceContainer;
import core.Type;
import items.upgrade.EvolveUpgrade;
import items.upgrade.Upgrade;

import java.util.List;
import java.util.Set;

public abstract class Decorator<T extends GameObject> extends GameObject{

    private final T object;

    public Decorator(T obj) {
        super(obj.getPlayer(), obj.getLocation(), obj.getValue(Options.SIZE_KEY), obj.getValue(Options.SIGHT_KEY));
        object = obj;
    }

    @Override
    public ResourceContainer getResources(Options option) {
        return object.getResources(option);
    }

    @Override
    public boolean checkStatus(Options option) {
        return object.checkStatus(option);
    }

    @Override
    public void perform(Options option) {
        object.perform(option);
    }

    @Override
    public int getValue(Options option) {
        return object.getValue(option);
    }

    @Override
    public void changeValue(Options option, int amount) {
        object.changeValue(option, amount);
    }

    @Override
    public T getObject(Type description) { return object; }

    @Override
    public String getType() {
        return object.getType();
    }

    @Override
    public String getToken() {
        return object.getToken();
    }

    @Override
    public Location getLocation() {
        return object.getLocation();
    }

    @Override
    public void setLocation(Location loc) {
        object.setLocation(loc);
    }

    @Override
    public List<Upgrade> getUpgrades() {
        return object.getUpgrades();
    }

    @Override
    public List<GameObject> getProducts() {
        return object.getProducts();
    }

    @Override
    public List<EvolveUpgrade> getEvolutions() {
        return object.getEvolutions();
    }

    @Override
    public Set<Type> getDescriptions() {
        return object.getDescriptions();
    }

    @Override
    public void updateDescriptions(Type... descr) {
        object.updateDescriptions(descr);
    }

    @Override
    public int getObjectIdentifier() {
        return object.getObjectIdentifier();
    }

    @Override
    public int hashCode() {
        return object.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return object.equals(obj);
    }

    @Override
    public String toString() {
        return object.toString();
    }
}
