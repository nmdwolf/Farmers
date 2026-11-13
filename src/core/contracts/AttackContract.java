package core.contracts;

import objects.Aggressive;
import objects.GameObject;
import objects.Operational;

public class AttackContract extends Contract {

    private final GameObject target;

    public <T extends Aggressive> AttackContract(T aggressor, int energyCost,
                                                                            GameObject target) {
        super(aggressor, energyCost);
        this.target = target;
    }

    @Override
    public boolean work(Logger logger) {
        if(super.work(logger)) {
            ((Aggressive)getEmployee()).attack(target);     // employer will always be Aggressor in this class
            boolean result = target.getHealth() <= 0;
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
