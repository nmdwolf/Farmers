package objects.buildings;

import core.resources.ResourceContainer;
import core.resources.Source;
import objects.templates.ConstructionTemplate;

public abstract class ResourceBuilding<B extends ResourceBuilding<B>> extends IdleBuilding<B> implements Source {

    public ResourceBuilding(ConstructionTemplate temp) {
        super(temp);

        if(getLoadout(objects.loadouts.Source.class).isEmpty())
            throw new IllegalArgumentException("Template does not contain a source attribute.");
    }

    @Override
    public ResourceContainer getSources() {
        return getLoadout(objects.loadouts.Source.class).get().getSources();
    }

}
