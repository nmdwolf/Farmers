package objects.buildings;

import objects.Construction;
import objects.templates.ConstructionTemplate;

import static core.InternalSettings.BUILDING_TYPE;

public class Building<B extends Building<B>> extends Construction<B> {

    public Building(ConstructionTemplate temp) {
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
