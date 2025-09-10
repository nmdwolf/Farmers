package objects.buildings;

import objects.Construction;
import objects.templates.ConstructionTemplate;
import objects.templates.TemplateFactory;
import objects.templates.UnitTemplate;

import static core.GameConstants.BUILDING_TYPE;

public class Building extends Construction {

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

    public static Building createBuilding(String className) throws IllegalArgumentException {
        if(TemplateFactory.isRegistered(className))
            return new Building((ConstructionTemplate) TemplateFactory.getTemplate(className));
        throw new IllegalArgumentException("The provided class " + className + " is unknown.");
    }
}
