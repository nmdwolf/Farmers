package items.buildings;

import static core.Option.*;

import core.*;

import general.OperationsList;
import general.ResourceContainer;
import items.Constructable;

import java.util.Map;

public abstract class Building extends Constructable {

    public Building(Player p, Cell cell, int size, ResourceContainer cost, int difficulty, Map<Option, Integer> params) {
        super(p, cell, size, cost, difficulty, true, params);

        if(!params.containsKey(CONSTRUCT))
            throw new IllegalArgumentException("No construction parameter specified.");

        setValue(CONTRACT, 0);
    }

    @Override
    public OperationsList getOperations(Option... options) {
        return new OperationsList();
    }

    @Override
    public void perform(Option option) {
        switch(option) {
            case CONSTRUCT:
                if(getValue(CONSTRUCT) - getValue(CONTRACT) == 1) {
                    if(getPlayer().hasResources(getResources(CONSTRUCT))) {
                        getPlayer().changeResources(getCost());
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
            case CONSTRUCT: yield getValue(CONTRACT) == 0 ? getPlayer().hasResources(getCost()) : getValue(CONTRACT) == getValue(CONSTRUCT);
            default: yield super.checkStatus(option);
        };
    }

    @Override
    public String toString() {
        return "Type: " + getClassLabel() + "\nPlayer: " + getPlayer().getName() +
                "\nHealth: " + getValue(HEALTH) + "/" + getValue(MAX_HEALTH);
    }
}
