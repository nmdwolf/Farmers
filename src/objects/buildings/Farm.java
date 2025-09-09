package objects.buildings;

import core.player.Award;
import core.resources.ResourceContainer;
import objects.templates.ConstructionTemplate;
import objects.templates.TemplateFactory;
import objects.templates.UnitTemplate;

import java.util.Optional;

public class Farm extends ResourceBuilding {


    public final static Award BUILT_AWARD = Award.createFreeAward("Food is ours.");

    public Farm() {
        super((ConstructionTemplate) TemplateFactory.getTemplate("Farm"));
    }

    @Override
    public Optional<Award> getConstructionAward() {
        return Optional.of(BUILT_AWARD);
    }

    @Override
    public String getResourceType() {
        return "Food";
    }

    @Override
    public String getClassLabel() {
        return "Farm";
    }

    @Override
    public String getToken() {
        return "F";
    }
}
