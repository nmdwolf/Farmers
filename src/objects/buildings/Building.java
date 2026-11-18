package objects.buildings;

import objects.GameObject;
import objects.Operational;
import objects.templates.Template;

import static core.InternalSettings.BUILDING_TYPE;

public class Building extends GameObject {

    public Building(Template temp) {
        super(temp);
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
