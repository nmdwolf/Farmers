package objects.buildings;

import UI.OperationsList;
import core.*;
import core.player.Player;
import objects.Constructor;
import objects.resources.ResourceContainer;

public abstract class ConstructiveBuilding extends Building implements Constructor {

    private int x, y;
    public ConstructiveBuilding(Player p, Cell cell, int cycle, int space, int sight, int health,
                                int degradeTime, int degradeAmount,
                                ResourceContainer cost, int difficulty, int x, int y) {
        super(p, cell, cycle, space, sight, health, degradeTime, degradeAmount, cost, difficulty);
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public OperationsList getOperations(int cycle, OperationCode code) {
        if(code == OperationCode.CONSTRUCTION)
            return getConstructions(cycle);
        else
            return new OperationsList();
    }
}
