package objects;

import core.Cell;
import core.OperationCode;
import core.Player;
import general.OperationsList;
import objects.resources.ResourceContainer;

public abstract class Constructor extends Operational {

    public Constructor(Player player, Cell cell, int cycle, int space, int sight, int health,
                       int degradeTime, int degradeAmount, int cycleLength,
                       ResourceContainer cost, int difficulty, boolean hasVisibileFoundation) {
        super(player, cell, cycle, space, sight, health, degradeTime, degradeAmount, cycleLength,
                cost, difficulty, hasVisibileFoundation);
    }

}
