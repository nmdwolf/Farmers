package objects.resources;

import core.Cell;
import core.player.Player;
import objects.GameObject;

public abstract class NaturalResource extends GameObject implements Source {

    private final ResourceContainer yield;

    public NaturalResource(Player player, Cell cell, int cycle, int space, ResourceContainer yield) {
        super(player, cell, cycle, space, 0, 999, 0, 0);
        this.yield = yield;
    }

    public ResourceContainer getYield() {
        return yield;
    }

    @Override
    public void cycle(int cycle) {
        super.cycle(cycle);
        getCell().changeResources(yield);
    }
}
