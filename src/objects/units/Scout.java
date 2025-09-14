package objects.units;

import core.*;
import core.OperationsList;
import core.player.Award;
import core.resources.ResourceContainer;
import objects.Evolvable;
import core.upgrade.EvolveUpgrade;
import objects.templates.TemplateFactory;
import objects.templates.UnitTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Scout extends Unit<Scout> implements Evolvable {

    public final static Award BUILT_AWARD = Award.createFreeAward("You can look for new lands.");
    public final static Award EVOLVE_AWARD = Award.createFreeAward("Exploring will be even easier now.");

    public final static ResourceContainer LEVEL1_COST = new ResourceContainer(new String[]{"Food", "Water", "Time"}, new int[]{200, 200, 10});

    private int level;

    public Scout() {
        super((UnitTemplate) TemplateFactory.getTemplate("Scout"));
        level = 1;
    }

    @Override
    public String getClassLabel() {
        return "Scout";
    }

    @Override
    public String getToken() {
        return "s";
    }

    @Override
    public @NotNull Optional<Award> getEvolveAward() {
        if(getLevel() == 2)
            return Optional.of(EVOLVE_AWARD);
        else
            return Optional.empty();
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        OperationsList list = new OperationsList();
        if(code == OperationCode.EVOLVE) {
            list.putUpgrade("Evolve", new EvolveUpgrade<>(this, LEVEL1_COST, 0, _ -> {
                changeSight(1);
                changeMaxEnergy(5);
                changeMaxHealth(20);
            }));
        }
        return list;
    }

    @Override
    public Optional<Award> getConstructionAward() {
        return Optional.of(BUILT_AWARD);
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void increaseLevel() {
        level++;
    }
}
