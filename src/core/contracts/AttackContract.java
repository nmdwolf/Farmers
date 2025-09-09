package core.contracts;

import objects.Aggressive;
import objects.GameObject;

public class AttackContract extends Contract {

    private final GameObject target;

    public AttackContract(Aggressive aggressor, int energyCost, GameObject target) {
        super((GameObject)aggressor, energyCost);
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
