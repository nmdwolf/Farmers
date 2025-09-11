package core.contracts;

import objects.Construction;
import objects.buildings.Foundation;
import objects.Spacer;
import objects.units.Worker;

public class ConstructContract<T extends Construction> extends Contract<Worker>{

    private final T constructable;
    private final Foundation<T> foundation;

    public ConstructContract(Worker employee, T constructable) {
        super(employee, constructable.getCompletion());
        this.constructable = constructable;
        foundation = new Foundation<>(constructable, this);
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
        constructable.initialize(foundation.getPlayer(), foundation.getCell(), foundation.getStartCycle());
        getEmployee().getPlayer().removeObject(foundation);
        getEmployee().getPlayer().addObject(constructable);
        if (constructable instanceof Spacer)
            getEmployee().getPlayer().changePopCap(((Spacer) constructable).getSpaceBoost());
        constructable.getConstructionAward().ifPresent(a -> getEmployee().getPlayer().getAwardArchive().awardExternal(a));
    }

    @Override
    public boolean work() {
        super.work();
        if(isStarted()) {
            if (((Worker)getEmployee()).getEnergy() >= constructable.getEnergyCost()) {
                constructable.construct();
                foundation.changeHealth(Math.divideExact(constructable.getMaxHealth(), constructable.getConstructionTime()));
                ((Worker)getEmployee()).changeEnergy(constructable.getEnergyCost());
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
        return constructable.getEnergyCost();
    }

    public Foundation<T> getFoundation() {
        return foundation;
    }
}
