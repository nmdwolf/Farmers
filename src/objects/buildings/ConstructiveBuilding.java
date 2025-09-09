package objects.buildings;

import core.OperationsList;
import core.*;
import core.player.Player;
import objects.Constructor;
import core.resources.ResourceContainer;
import objects.templates.ConstructionTemplate;

public abstract class ConstructiveBuilding extends Building implements Constructor {

    private int x, y;
    public ConstructiveBuilding(ConstructionTemplate temp, int x, int y) {
        super(temp);
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
