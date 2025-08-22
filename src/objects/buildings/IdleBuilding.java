package objects.buildings;

import core.player.Award;
import core.Cell;
import core.OperationCode;
import core.player.Player;
import UI.OperationsList;
import objects.resources.ResourceContainer;
import org.jetbrains.annotations.NotNull;

public abstract class IdleBuilding extends Building {

    public IdleBuilding(Player p, Cell cell, int cycle, int space, int sight,
                        int health, int degradeTime, int degradeAmount, ResourceContainer cost, int difficulty) {
        super(p, cell, cycle, space, sight, health, degradeTime, degradeAmount, cost, difficulty);
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        return new OperationsList();
    }

    @Override
    public void cycle(int cycle) {}

    @Override
    public @NotNull Award getEvolveAward() {
        return null;
    }
}
