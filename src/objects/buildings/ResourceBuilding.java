package objects.buildings;

import core.resources.ResourceContainer;
import core.resources.Source;
import objects.templates.ConstructionTemplate;

public abstract class ResourceBuilding extends IdleBuilding implements Source {

    private ResourceContainer gains;

    public ResourceBuilding(ConstructionTemplate temp) {
        super(temp);
        this.gains = gains; // TODO
    }

    @Override
    public ResourceContainer getYield() {
        return gains;
    }

}
