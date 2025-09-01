package core.contracts;

import objects.Constructable;
import objects.buildings.Foundation;
import objects.Spacer;
import objects.units.Unit;
import objects.units.Worker;

public class ConstructContract<T extends Constructable> extends Contract{

    private final T constructable;
    private final Foundation<T> foundation;

    public ConstructContract(Worker employee, T constructable) {
        super(employee, constructable.getCompletion());
        this.constructable = constructable;
        foundation = new Foundation<>(employee.getPlayer(), constructable, this, constructable.hasVisibleFoundation(), constructable.getStartCycle());
    }

    @Override
    public void initialize() {
        if(!isStarted() && getEmployee().getPlayer().hasResources(constructable.getCost())) {
            getEmployee().getPlayer().changeResources(constructable.getCost().negative());
            getEmployee().getPlayer().addObject(foundation);
            getEmployee().setTarget(foundation);
            super.initialize();
        }
    }

    @Override
    public void terminate() {
        getEmployee().getPlayer().removeObject(foundation);
        getEmployee().getPlayer().addObject(constructable);
        if (constructable instanceof Spacer)
            getEmployee().getPlayer().changePopCap(((Spacer) constructable).getSpaceBoost());
        if (constructable instanceof Unit)
            getEmployee().getPlayer().changePopCap(constructable.getSpace());
        constructable.getConstructionAward().ifPresent(a -> getEmployee().getPlayer().getAwardArchive().awardExternal(a));
    }

    @Override
    public void abandon() {
        getEmployee().setTarget(null);
    }

    @Override
    public boolean work() {
        super.work();

        if(isStarted()) {
            if (getEmployee().getEnergy() >= constructable.getDifficulty()) {
                constructable.construct();
                getEmployee().changeEnergy(constructable.getDifficulty());
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
        return constructable.getDifficulty();
    }
}
