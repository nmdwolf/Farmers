package objects.buildings;

import core.player.Award;
import objects.Booster;
import objects.GameObject;
import objects.templates.ConstructionTemplate;
import objects.templates.TemplateFactory;

import java.util.Optional;

public class Lumberjack extends IdleBuilding implements Booster {

    public final static Award BUILT_AWARD = Award.createFreeAward("You figured out how to chop wood.");

    public Lumberjack() {
        super((ConstructionTemplate) TemplateFactory.getTemplate("Lumberjack"));
    }

    @Override
    public int getBoostRadius() { return 2; }

    @Override
    public int getBoostAmount(GameObject obj, String res) {
        if(res.equals("Wood"))
            return 2;
        else
            return 0;
    }

    @Override
    public Optional<Award> getConstructionAward() {
        return Optional.of(BUILT_AWARD);
    }
}
