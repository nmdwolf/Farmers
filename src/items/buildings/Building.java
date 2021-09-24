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

        updateTypes(Type.BUILDING);
    }

    @Override
    public ResourceContainer getResources(Option option) {
        return switch(option) {
            case CONSTRUCT: yield cost;
            default: yield ResourceContainer.EMPTY_CONTAINER;
        };
    }

    @Override
    public OperationsList getOperations() {
        return null;
    }

    @Override
    public void perform(Option option) {
        switch(option) {
            case CONSTRUCT:
                getPlayer().changeResources(cost);
                getPlayer().addObject(this);
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
    public String toString() {
        return "Type: " + getClassIdentifier() + "\nPlayer: " + player.getName() +
                "\nHealth: " + getValue(HEALTH) + "/" + getValue(MAX_HEALTH);
    }
}
