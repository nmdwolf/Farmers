package objects.buildings;

import core.resources.ResourceContainer;
import core.resources.Source;
import objects.templates.Template;

public abstract class ResourceBuilding extends Building implements Source {

    public ResourceBuilding(Template temp) {
        super(temp);

        if(getLoadout(objects.loadouts.Source.class).isEmpty())
            throw new IllegalArgumentException("Template does not contain a source attribute.");
    }

    @Override
    public ResourceContainer getSources() {
        return getLoadout(objects.loadouts.Source.class).orElseThrow().getSources();
    }

}
