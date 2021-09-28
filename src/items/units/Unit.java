package items.units;

import static core.Option.*;

import core.*;
import general.OperationsList;
import general.ResourceContainer;
import items.GameObject;

import java.util.Iterator;
import java.util.Map;

public abstract class Unit extends GameObject {

    private final ResourceContainer cost;

    public Unit(Player p, Location loc, ResourceContainer cost, Map<Option, Integer> params) {
        super(p, loc, params);

        this.cost = cost;

        if(!params.containsKey(MAX_ENERGY))
            throw new IllegalArgumentException("Missing energy parameter");
        if(!params.containsKey(ANIMATION))
            throw new IllegalArgumentException("Missing animation parameter");

        changeValue(ENERGY, params.get(MAX_ENERGY));

        updateTypes(Type.UNIT);
    }

    @Override
    public void changeValue(Option option, int amount) {
        if(option == MAX_ENERGY) {
            changeValue(MAX_ENERGY, amount);
            changeValue(ENERGY, amount);
        } else
            super.changeValue(option, amount);
    }

    @Override
    public void perform(Option option) {
        switch(option) {
            case CONSTRUCT:
                getPlayer().changeResources(cost);
                getPlayer().changePop(getValue(SIZE));
                getPlayer().addObject(this);
                break;
            case TOTAL_CYCLE:
                super.perform(TOTAL_CYCLE);
                setValue(ENERGY, getValue(MAX_ENERGY));
                break;
            default:
                super.perform(option);
                break;
        }
    }

    @Override
    public boolean checkStatus(Option option) {
        return switch(option) {
            case CONSTRUCT: yield player.hasResources(cost);
            default: yield super.checkStatus(option);
        };
    }

    @Override
    public ResourceContainer getResources(Option option) {
        return switch(option) {
            case CONSTRUCT: yield cost;
            default: yield ResourceContainer.EMPTY_CONTAINER;
        };
    }

    @Override
    public OperationsList getOperations(Option... options) {
        OperationsList operations = new OperationsList();
        for (Iterator<GameObject> it = getPlayer().getObjects().stream().filter(obj -> obj.getLocation().equals(getLocation())
                && obj.getTypes().contains(Type.HEALER)).iterator(); options.length == 0 && it.hasNext(); ) {
            GameObject object = it.next().castAs(Type.HEALER);
            operations.put("Heal at " + object.getToken(), (obj, params) -> {
                changeValue(STATUS, GameConstants.HEALING_STATUS);
                changeValue(HEAL, object.getValue(HEAL));
            });
        }
        return operations;
    }

    @Override
    public String toString() {
        return "Type: " + getClassIdentifier() + "\nPlayer: " + player.getName() +
                "\nHealth: " + getValue(HEALTH) + "/" + getValue(MAX_HEALTH) +
                "\nEnergy: " + getValue(ENERGY) + "/" + getValue(MAX_ENERGY);
    }
}
