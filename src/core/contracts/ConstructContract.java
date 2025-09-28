package core.contracts;

import objects.Construction;
import objects.buildings.Foundation;
import objects.Spacer;
import objects.units.Worker;

public class ConstructContract<T extends Construction<T>> extends Contract<Worker>{

    private final T constructable;
    private final Foundation<T> foundation;

    public ConstructContract(Worker employee, T constructable) {
        super(employee, constructable.getEnergyCost());
        this.constructable = constructable;
        foundation = new Foundation<>(constructable, this);
    }

    @Override
    public void initialize() {
        if(!isStarted() && getEmployee().getPlayer().hasResources(constructable.getCost())
                && getEmployee().getPlayer().addObject(foundation, getEmployee().getCell())) {
            getEmployee().getPlayer().changeResources(constructable.getCost().negative());
            super.initialize();
        }
    }

    @Override
    public void terminate() {
//        constructable.initialize(foundation.getPlayer(), foundation.getStartCycle());
        getEmployee().getPlayer().removeObject(foundation);
        getEmployee().getPlayer().addObject(constructable, foundation.getCell());
        constructable.handleCompletion();
        constructable.getConstructionAward().ifPresent(a -> getEmployee().getPlayer().getAwardArchive().awardExternal(a));
    }

    @Override
    public boolean work(Logger logger) {
        if(super.work(logger)) {
            foundation.construct();
            foundation.alertChange();
        }

        logger.logConstruction(foundation.isComplete());
        if(foundation.isComplete()) {
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
