package items;

import core.*;
import general.OperationsList;
import general.ResourceContainer;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;

import static core.Option.*;

/**
 * Decorators are meant to equip GameObjects with extra disposable structure or properties
 * without changing the core data.
 * @param <T> type of underlying GameObject
 */
public abstract class Decorator<T extends GameObject> extends GameObject{

    private final T object;

    public Decorator(T obj) {
        super(obj.getPlayer(), obj.getCell(), obj.getSize(), new HashMap<>() {{
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
    public String getClassLabel() {
        return object.getClassLabel();
    }

    @Override
    public String getToken() {
        return object.getToken();
    }

    @Override
    public void setCell(Cell cell) {
        super.setCell(cell);
        object.setCell(cell);
    }

    public GameObject getObject() {
        if(object instanceof Decorator)
            return ((Decorator)object).getObject();
        else
            return object;
    }

    @Override
    public int getObjectIdentifier() {
        return object.getObjectIdentifier();
    }

    @Override
    public OperationsList getOperations(Option... options) {
        return object.getOperations(options);
    }

    @Override
    public Award getAward(Option option) {
        return object.getAward(option);
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
