package core.contracts;

import objects.Aggressive;
import objects.GameObject;
import objects.Operational;

public class AttackContract<T extends GameObject & Operational<T> & Aggressive> extends Contract<T> {

    private final GameObject target;

    public AttackContract(T aggressor, int energyCost, GameObject target) {
        super(aggressor, energyCost);
        this.target = target;
    }

    @Override
    public boolean work() {
        ((Aggressive)getEmployee()).attack(target);
        return super.work() && target.getHealth() <= 0;
    }

    @Override
    public void terminate() {}

    @Override
    public void abandon() {}

    public GameObject getTarget() {
        return target;
    }
}
