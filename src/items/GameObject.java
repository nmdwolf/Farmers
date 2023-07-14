package items;

import core.*;

import general.*;

import java.awt.image.BufferedImage;
import java.util.*;

import static core.Option.*;

public abstract class GameObject {

    private final int id;
    private final Map<Option, Integer> parameters;

    private Cell cell;
    private Player player;
    private int size;

    public GameObject(Player player, Cell cell, int size, Map<Option, Integer> params) {
        id = CustomMethods.getNewIdentifier();

        this.cell = cell;
        this.player = player;
        this.size = size;

        if(!params.containsKey(SIGHT) || !params.containsKey(SIZE)
                || !params.containsKey(MAX_HEALTH)
                || !params.containsKey(DEGRADATION_AMOUNT) || !params.containsKey(DEGRADATION_CYCLE))
            throw new IllegalArgumentException("Missing default parameter(s)");
        else
            parameters = params;

        if(!params.containsKey(STATUS))
            setValue(STATUS, GameConstants.IDLE_STATUS);

        parameters.put(LEVEL, 0);
        parameters.put(CYCLE, 0);
        parameters.put(TOTAL_CYCLE, 0);
        parameters.put(OLD_STATUS, parameters.get(STATUS));
        parameters.put(HEALTH, parameters.get(MAX_HEALTH));
    }

    public int getObjectIdentifier() { return id; }

    public Player getPlayer() { return player; }
    public Cell getCell() { return cell; }
    public void setCell(Cell cell) { this.cell = cell; }

    public abstract String getClassLabel();
    public abstract String getToken();
    public abstract BufferedImage getSprite();

    public abstract Award getAward(Option option);
    public abstract OperationsList getOperations(Option... options);

    public void perform(Option option) {
        switch(option) {
            case DESTROY:
                player.removeObject(this);
                break;
            case TOTAL_CYCLE:
                parameters.put(TOTAL_CYCLE, parameters.get(TOTAL_CYCLE) + 1);
                parameters.put(CYCLE, parameters.get(CYCLE) + 1);
                break;
            case DEGRADE:
                if(parameters.get(CYCLE) >= parameters.get(DEGRADATION_CYCLE))
                    changeValue(HEALTH, -parameters.get(DEGRADATION_AMOUNT));
                if(getValue(HEALTH) <= 0) {
                    perform(DESTROY);
                }
                break;
        }

    }
    public boolean checkStatus(Option option) { return (option == DESTROY && getValue(HEALTH) <= 0); }
    public int getValue(Option option) {
        Integer param = parameters.get(option);
        if(param == null)
            throw new IllegalArgumentException("Option " + option + " not recognized for this object.");
        return param;
    }
    public void changeValue(Option option, int amount) {
        switch(option) {
            case STATUS: case OLD_STATUS:
                setValue(option, amount);
                break;
            default:
                if(parameters.containsKey(option))
                    parameters.put(option, parameters.get(option) + amount);
                else
                    setValue(option, amount);
                break;
        }
    }
    public void setValue(Option option, int amount) {
        switch(option) {
            case STATUS:
                parameters.put(OLD_STATUS, parameters.get(STATUS));
                parameters.put(STATUS, amount);
                break;
            case MAX_HEALTH:
                parameters.put(MAX_HEALTH, amount);
                parameters.put(HEALTH, amount);
                break;
            default:
                parameters.put(option, amount);
                break;
        }
    }
    public int getSize() { return size; }
    public void changeSize(int amount) { size += amount; }
    public ResourceContainer getResources(Option option) { return ResourceContainer.EMPTY_CONTAINER; }

    // BASE METHODS INHERITED FROM OBJECT
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof GameObject) && (id == ((GameObject)obj).getObjectIdentifier());
    }
}
