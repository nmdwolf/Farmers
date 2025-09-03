package objects.buildings;

import core.Cell;
import core.OperationCode;
import core.player.Player;
import core.OperationsList;
import core.resources.ResourceContainer;

public abstract class IdleBuilding extends Building {

    public IdleBuilding(Player p, Cell cell, int cycle, int space, int sight,
                        int health, int degradeTime, int degradeAmount, ResourceContainer cost, int difficulty) {
        super(p, cell, cycle, space, sight, health, degradeTime, degradeAmount, cost, difficulty);
    }

    @Override
    public void cycle(int cycle) {}
}
