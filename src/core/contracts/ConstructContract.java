package core.contracts;

import objects.Construction;
import objects.buildings.Foundation;
import objects.Spacer;
import objects.units.Unit;
import objects.units.Worker;

public class ConstructContract<T extends Construction> extends Contract{

    private final T constructable;
    private final Foundation<T> foundation;

    public ConstructContract(Worker employee, T constructable) {
        super(employee, constructable.getCompletion());
        this.constructable = constructable;
        foundation = new Foundation<>(constructable, this, constructable.hasVisibleFoundation());
        foundation.initialize(employee.getPlayer(), employee.getCell(), constructable.getStartCycle());
    }

    @Override
    public void initialize() {
        if(!isStarted() && getEmployee().getPlayer().hasResources(constructable.getCost())) {
            getEmployee().getPlayer().changeResources(constructable.getCost().negative());
            getEmployee().getPlayer().addObject(foundation);
            super.initialize();
        }
    }

    @Override
    public void terminate() {
        getEmployee().getPlayer().removeObject(foundation);
        getEmployee().getPlayer().addObject(constructable);
        constructable.initialize(getFoundation().getPlayer(), getFoundation().getCell(), getFoundation().getStartCycle());
        if (constructable instanceof Spacer)
            getEmployee().getPlayer().changePopCap(((Spacer) constructable).getSpaceBoost());
        if (constructable instanceof Unit)
            getEmployee().getPlayer().changePopCap(constructable.getSize());
        constructable.getConstructionAward().ifPresent(a -> getEmployee().getPlayer().getAwardArchive().awardExternal(a));
    }

    @Override
    public boolean work() {
        super.work();
        if(isStarted()) {
            if (((Worker)getEmployee()).getEnergy() >= constructable.getAttackCost()) {
                constructable.construct();
                ((Worker)getEmployee()).changeEnergy(constructable.getAttackCost());
            }
        }

        if(constructable.isCompleted()) {
            terminate();
            return true;
        } else
            return false;
    }

    @Override
    public int getEnergyCost() {
        return constructable.getAttackCost();
    }

    public Foundation<T> getFoundation() {
        return foundation;
    }
}
