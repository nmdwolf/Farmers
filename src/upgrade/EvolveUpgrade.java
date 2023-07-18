package upgrade;

import core.Operation;
import resources.ResourceContainer;
import items.Evolvable;
import items.GameObject;

public class EvolveUpgrade<T extends GameObject & Evolvable> extends InstanceUpgrade<T> {

    private final Operation evolution;

    public EvolveUpgrade(T obj, ResourceContainer res, int cycleThreshold, Operation task) {
        super(obj, res, cycleThreshold);
        evolution = task;
    }

    @Override
    public void upgrade() {
        super.upgrade();
        getObject().increaseLevel();
        getObject().getPlayer().enableAward(getObject().getEvolveAward());
        evolution.perform();
    }

    @Override
    public String toString() {
        return "Evolve";
    }
}
