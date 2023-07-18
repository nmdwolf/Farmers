package resources;

import core.Cell;
import core.Player;
import items.GameObject;

public abstract class NaturalResource extends GameObject implements Source {

    private final ResourceContainer yield;

    public NaturalResource(Player player, Cell cell, int cycle, int space, ResourceContainer yield) {
        super(player, cell, cycle, space, 0, 999, 0, 0);
        this.yield = yield;
    }

    public ResourceContainer getYield() {
        return yield;
    }
}
