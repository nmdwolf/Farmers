package items.upgrade;

import core.Operation;
import core.Option;
import general.ResourceContainer;
import items.Evolvable;
import items.GameObject;

public class EvolveUpgrade<T extends GameObject & Evolvable> extends InstanceUpgrade<T> {

    private final Operation evolution;

    public EvolveUpgrade(T obj, ResourceContainer res, int cycleThreshold, Operation task) {
        super(obj, res, cycleThreshold);
        evolution = task;
    }

    @Override
    public void applyTo(T object) {
        object.changeValue(Option.LEVEL, 1);
        object.getPlayer().enable(object.getAward(Option.ENABLED));
        evolution.perform();
    }

    @Override
    public String toString() {
        return "Evolve";
    }
}
