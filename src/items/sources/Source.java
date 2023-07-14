package items.sources;

import core.Cell;
import core.Option;
import core.Player;
import general.OperationsList;
import general.ResourceContainer;
import items.GameObject;

import java.util.Map;

public abstract class Source extends GameObject {

    private boolean primed;

    private final ResourceContainer gain;

    public Source(Player player, Cell cell, int size, Map<Option, Integer> params, ResourceContainer gains) {
        super(player, cell, size, params);
        gain = gains;
    }

    @Override
    public ResourceContainer getResources(Option option) {
        if (option == Option.SOURCE) {
            primed = false;
            return gain;
        } else
            return this.getResources(option);
    }

    @Override
    public void perform(Option option) {
        if (option == Option.SOURCE)
            primed = true;
        else
            super.perform(option);
    }

    @Override
    public boolean checkStatus(Option option) {
        if(option == Option.SOURCE)
            return primed;
        else
            return super.checkStatus(option);
    }

    @Override
    public OperationsList getOperations(Option... options) {
        return new OperationsList();
    }
}
