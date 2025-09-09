package objects.buildings;

import objects.templates.ConstructionTemplate;

public abstract class IdleBuilding extends Building {

    public IdleBuilding(ConstructionTemplate temp) {
        super(temp);
    }

    @Override
    public void cycle(int cycle) {}
}
