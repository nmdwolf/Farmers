package items;

import core.Cell;
import core.Option;
import core.Player;
import general.ResourceContainer;

import java.util.Map;

import static core.Resource.TIME;

public abstract class Constructable extends GameObject{

    private int required;
    private final int difficulty;
    private final boolean hasVisibleFoundation;
    private final ResourceContainer cost;

    public Constructable(Player player, Cell cell, int size, ResourceContainer cost, int difficulty,
                         boolean hasVisibleFoundation, Map<Option, Integer> params) {
        super(player, cell, size, params);
        this.cost = cost;
        this.required = cost.get(TIME);
        this.difficulty = difficulty;
        this.hasVisibleFoundation = hasVisibleFoundation;
    }

    public int getRequired() {
        return required;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public ResourceContainer getResources(Option option) {
        if(option == Option.CONSTRUCT)
            return cost;
        else
            return super.getResources(option);
    }

    public boolean HasVisibleFoundation() {
        return hasVisibleFoundation;
    }

    public ResourceContainer getCost() { return cost; }
}
