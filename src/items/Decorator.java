package items;

import core.Location;
import core.Option;
import core.Player;
import general.OperationsList;
import general.ResourceContainer;
import core.Type;
import general.TypeException;
import general.TypedConsumer;
import items.upgrade.EvolveUpgrade;
import items.upgrade.Upgrade;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static core.Option.*;

public abstract class Decorator<T extends GameObject> extends GameObject{

    private final T object;

    public Decorator(T obj) {
        super(obj.getPlayer(), obj.getLocation(), new HashMap<>() {{
            put(SIZE, obj.getValue(SIZE));
            put(SIGHT, obj.getValue(SIGHT));
            put(STATUS, obj.getValue(STATUS));
            put(MAX_HEALTH, obj.getValue(MAX_HEALTH));
            put(DEGRADATION_CYCLE, obj.getValue(DEGRADATION_CYCLE));
            put(DEGRADATION_AMOUNT, obj.getValue(DEGRADATION_AMOUNT));
        }});
        object = obj;
    }

    @Override
    public BufferedImage getSprite() {
        return object.getSprite();
    }

    @Override
    public Player getPlayer() {
        return object.getPlayer();
    }

    @Override
    public ResourceContainer getResources(Option option) {
        return object.getResources(option);
    }

    @Override
    public boolean checkStatus(Option option) {
        return object.checkStatus(option);
    }

    @Override
    public void perform(Option option) {
        object.perform(option);
    }

    @Override
    public int getValue(Option option) {
        return object.getValue(option);
    }

    @Override
    public void changeValue(Option option, int amount) {
        object.changeValue(option, amount);
    }

    @Override
    public void setValue(Option option, int amount) {
        object.setValue(option, amount);
    }

    @Override
    public GameObject castAs(Type description) { return object.castAs(description); }

    @Override
    public String getClassIdentifier() {
        return object.getClassIdentifier();
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
    public Set<Type> getTypes() {
        Set<Type> types = new HashSet<>(object.getTypes());
        types.addAll(super.getTypes());
        return types;
    }

    @Override
    public int getObjectIdentifier() {
        return object.getObjectIdentifier();
    }

    @Override
    public void typedDo(Type type, TypedConsumer toRun) throws TypeException {
        object.typedDo(type, toRun);
    }

    @Override
    public OperationsList getOperations() {
        return object.getOperations();
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
