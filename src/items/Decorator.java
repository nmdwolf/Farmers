package items;

import core.Location;
import core.Player;

import java.util.Set;

public abstract class Decorator<T extends GameObject> implements GameObject{

    private final T object;

    public Decorator(T obj) {
        object = obj;
    }

    @Override
    public T getObject(int description) { return object; }

    @Override
    public Location getLocation() {
        return object.getLocation();
    }

    @Override
    public Player getPlayer() {
        return object.getPlayer();
    }

    @Override
    public String getType() {
        return object.getType();
    }

    @Override
    public String getToken() {
        return object.getToken();
    }

    @Override
    public int getSize() {
        return object.getSize();
    }

    @Override
    public void cycle(int cycle) {
        object.cycle(cycle);
    }

    @Override
    public Set<Integer> getDescriptions() {
        return object.getDescriptions();
    }

    @Override
    public void updateDescriptions(int... descriptions) { object.updateDescriptions(descriptions); }

    @Override
    public int getLineOfSight() {
        return object.getLineOfSight();
    }

    @Override
    public void changeLineOfSight(int amount) {
        object.changeLineOfSight(amount);
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
