package objects.buildings;

import core.OperationCode;
import core.OperationsList;
import objects.Constructor;
import objects.Operational;
import objects.templates.Template;

import static core.InternalSettings.BUILDING_TYPE;


public abstract class OperationalBuilding extends Operational {

    private int x, y;
    public OperationalBuilding(Template temp, int x, int y) {
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

    @Override
    public String toString() {
        return "Type: " + getClassLabel() + "\nPlayer: " + getPlayer().getName() +
                "\nHealth: " + getHealth() + "/" + getMaxHealth() + super.toString();
    }

    @Override
    public int getType() {
        return BUILDING_TYPE;
    }

}
