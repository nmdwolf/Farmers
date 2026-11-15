package core.contracts;

import objects.Construction;
import objects.GameObject;
import objects.buildings.Foundation;
import objects.units.Worker;

public class ConstructContract<T extends Construction> extends Contract{

    private final T constructable;
    private final Foundation<T> foundation;

    public ConstructContract(Worker employee, T constructable) {
        super(employee, constructable.getEnergyCost());
        this.constructable = constructable;
        foundation = new Foundation<>(constructable, this);
    }

    @Override
    public void initialize() {
        if(!isStarted() && ((GameObject)getEmployee()).getPlayer().hasResources(constructable.getCost())
                && ((GameObject)getEmployee()).getPlayer().addObject(foundation, ((GameObject)getEmployee()).getCell())) {
            ((GameObject)getEmployee()).getPlayer().changeResources(constructable.getCost().negative());
            super.initialize();
        }
    }

    @Override
    public void terminate() {
//        constructable.initialize(foundation.getPlayer(), foundation.getStartCycle());
        ((GameObject)getEmployee()).getPlayer().removeObject(foundation);
        ((GameObject)getEmployee()).getPlayer().addObject(constructable, foundation.getCell());
        constructable.handleCompletion();
        constructable.getConstructionAward().ifPresent(a -> ((GameObject)getEmployee()).getPlayer().getAwardArchive().awardExternal(a));
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
