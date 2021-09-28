package items.buildings;

import static core.Option.*;

import core.*;

import general.OperationsList;
import general.ResourceContainer;
import items.GameObject;

import java.util.Map;

public abstract class Building extends GameObject {

    private final ResourceContainer cost;

    public Building(Player p, Location loc, ResourceContainer cost, Map<Option, Integer> params) {
        super(p, loc, params);
        this.cost = cost;

        if(!params.containsKey(CONSTRUCT))
            throw new IllegalArgumentException("No construction parameter specified.");

        updateTypes(Type.BUILDING);
        setValue(CONTRACT, 0);
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
        return null;
    }

    @Override
    public void perform(Option option) {
        switch(option) {
            case CONSTRUCT:
                if(getValue(CONSTRUCT) - getValue(CONTRACT) == 1) {
                    if(getPlayer().hasResources(getResources(CONSTRUCT))) {
                        getPlayer().changeResources(cost);
                        getPlayer().addObject(this);
                        changeValue(CONTRACT, 1);
                    }
                } else
                    changeValue(CONTRACT, 1);
                break;
            default:
                super.perform(option);
                break;
        }
    }

    @Override
    public boolean checkStatus(Option option) {
        return switch(option) {
            case CONSTRUCT: yield getValue(CONTRACT) == 0 ? player.hasResources(cost) : getValue(CONTRACT) == getValue(CONSTRUCT);
            default: yield super.checkStatus(option);
        };
    }

    @Override
    public String toString() {
        return "Type: " + getClassIdentifier() + "\nPlayer: " + player.getName() +
                "\nHealth: " + getValue(HEALTH) + "/" + getValue(MAX_HEALTH);
    }
}
