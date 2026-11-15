package core.contracts;

import objects.GameObject;
import objects.Operational;
import objects.loadouts.Fighter;

public class AttackContract extends Contract {

    private final GameObject target;

    public <T extends Operational> AttackContract(T aggressor, GameObject target) {
        super(aggressor, ((GameObject)aggressor).getLoadout(Fighter.class).get().getAttackCost());
        this.target = target;

        if (!((GameObject)aggressor).hasLoadout(Fighter.class))
            throw new IllegalArgumentException("Only GameObjects carrying a Fighter loadout are expected.");
    }

    @Override
    public boolean work(Logger logger) {
        if(super.work(logger)) {
            ((GameObject)getEmployee()).dispatchToLoadout(Fighter.class, f -> f.attack(target));
            boolean result = (target.getHealth() <= 0);
            logger.logAttack(result);
            return result;
        }
        return false;
    }

    @Override
    public void terminate() {}

    @Override
    public void abandon() {}

    public GameObject getTarget() {
        return target;
    }
}
