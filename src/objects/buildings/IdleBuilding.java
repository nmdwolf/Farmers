package objects.buildings;

import core.Award;
import core.Cell;
import core.Player;
import general.OperationsList;
import objects.resources.ResourceContainer;
import objects.Constructable;

public abstract class IdleBuilding extends Constructable {

    public IdleBuilding(Player p, Cell cell, int cycle, int space, int sight,
                        int health, int degradeTime, int degradeAmount, ResourceContainer cost, int difficulty) {
        super(p, cell, cycle, space, sight, health, degradeTime, degradeAmount, cost, difficulty, true);
    }

    @Override
    public OperationsList getOperations(int cycle) {
        return new OperationsList();
    }

    @Override
    public void cycle(int cycle) {

    }

    @Override
    public OperationsList getEvolutions(int cycle) {
        return new OperationsList();
    }

    @Override
    public Award getEvolveAward() {
        return null;
    }
}
