package core.contracts;

import items.Constructable;
import items.Foundation;
import items.units.Worker;

import static core.Option.CONSTRUCT;

public class ConstructContract<T extends Constructable> extends Contract{

    private final T constructable;
    private final Foundation<T> foundation;

    public ConstructContract(Worker employee, T constructable) {
        super(employee, constructable.getRequired());
        this.constructable = constructable;
        foundation = new Foundation(employee.getPlayer(), constructable, constructable.HasVisibleFoundation());
    }

    @Override
    public void initialize() {
        getEmployee().getPlayer().changeResources(constructable.getResources(CONSTRUCT));
        getEmployee().getPlayer().addObject(foundation);
    }

    @Override
    public void terminate() {
        getEmployee().getPlayer().removeObject(foundation);
        getEmployee().getPlayer().addObject(constructable);
    }

    @Override
    public boolean work() {
        constructable.perform(CONSTRUCT);
        if(constructable.checkStatus(CONSTRUCT))
            foundation.perform(CONSTRUCT);
        return super.work();
    }

    @Override
    public int getEnergyCost() {
        return constructable.getDifficulty();
    }
}
