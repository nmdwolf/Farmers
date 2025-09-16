package core.upgrade;

import core.Operation;
import core.player.Player;
import core.resources.ResourceContainer;
import objects.Evolvable;
import objects.GameObject;

public class EvolveUpgrade<T extends GameObject<T> & Evolvable> extends InstanceUpgrade<T> {

    private final Operation evolution;

    public EvolveUpgrade(T obj, ResourceContainer res, int cycleThreshold, Operation task) {
        super(obj, res, cycleThreshold);
        evolution = task;
    }

    @Override
    public void upgrade(Player p) {
        super.upgrade(p);
        getObject().increaseLevel();
        getObject().getEvolveAward().ifPresent(a -> getObject().getPlayer().getAwardArchive().awardExternal(a));
        evolution.perform(null);
    }

    @Override
    public String toString() {
        return "Evolve";
    }
}
