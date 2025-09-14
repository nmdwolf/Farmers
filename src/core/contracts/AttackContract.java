package core.contracts;

import objects.Aggressive;
import objects.GameObject;
import objects.Operational;

public class AttackContract<T extends GameObject<T> & Operational<T> & Aggressive> extends Contract<T> {

    private final GameObject<?> target;

    public AttackContract(T aggressor, int energyCost, GameObject<?> target) {
        super(aggressor, energyCost);
        this.target = target;
    }

    @Override
    public boolean work(Logger logger) {
        if(super.work(logger)) {
            getEmployee().attack(target);
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

    public GameObject<?> getTarget() {
        return target;
    }
}
