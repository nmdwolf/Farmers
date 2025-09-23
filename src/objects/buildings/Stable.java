package objects.buildings;

import core.*;
import core.OperationsList;
import core.player.Award;
import objects.templates.ConstructionTemplate;
import objects.templates.TemplateFactory;

import java.util.Optional;

public class Stable extends ConstructiveBuilding<Stable> {

    public final static Award BUILT_AWARD = Award.createFreeAward("Yee-haw!");

    public final static int STABLE_X = 0;
    public final static int STABLE_Y = 0;

    public final static String TOKEN = "S";

    public Stable() {
        super((ConstructionTemplate) TemplateFactory.getTemplate("Stable"), STABLE_X, STABLE_Y);
    }

    @Override
    public Optional<Award> getConstructionAward() {
        return Optional.of(BUILT_AWARD);
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        return new OperationsList();
    }

    @Override
    public OperationsList getConstructions(int cycle) {
        return new OperationsList();
    }

    @Override
    public void cycle(int cycle) {

    }
}
