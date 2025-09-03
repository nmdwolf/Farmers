package core.contracts;

import objects.GameObject;
import objects.loadouts.Fighter;

public class AttackContract extends Contract {

    private final GameObject target;

    public AttackContract(GameObject aggr, int energyCost, GameObject target) {
        super(aggr, energyCost);
        this.target = target;
    }

    @Override
    public boolean work() {
        getEmployee().getLoadout(Fighter.class).ifPresent(fighter -> target.changeHealth(-fighter.getAttack()));
        return super.work() && target.getHealth() <= 0;
    }

    @Override
    public void terminate() {}

    @Override
    public void abandon() {}
}
