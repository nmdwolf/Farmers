package objects.buildings;

import objects.templates.ConstructionTemplate;

public abstract class IdleBuilding<B extends IdleBuilding<B>> extends Building<B> {

    public IdleBuilding(ConstructionTemplate temp) {
        super(temp);
    }

    @Override
    public void cycle(int cycle) {}
}
