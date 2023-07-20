package core.contracts;

import objects.Constructable;
import objects.Foundation;
import objects.Spacer;
import objects.units.Unit;
import objects.units.Worker;

public class ConstructContract<T extends Constructable> extends Contract{

    private final T constructable;
    private final Foundation<T> foundation;

    public ConstructContract(Worker employee, T constructable) {
        super(employee, constructable.getCompleted());
        this.constructable = constructable;
        foundation = new Foundation<>(employee.getPlayer(), constructable, constructable.hasVisibleFoundation(), constructable.getStartCycle());
    }

    @Override
    public void initialize() {
        getEmployee().getPlayer().changeResources(constructable.getCost().negative());
        getEmployee().getPlayer().addObject(foundation);
    }

    @Override
    public void terminate() {
        getEmployee().getPlayer().removeObject(foundation);
        getEmployee().getPlayer().addObject(constructable);
        if (constructable instanceof Spacer)
            getEmployee().getPlayer().changePopCap(((Spacer) constructable).getSpaceBoost());
        if (constructable instanceof Unit)
            getEmployee().getPlayer().changePopCap(constructable.getSpace());
        getEmployee().getPlayer().enableAward(constructable.getConstructionAward());
    }

    @Override
    public void abandon() {
        getEmployee().getPlayer().removeObject(foundation);
    }

    @Override
    public boolean work() {
        if(getEmployee().getEnergy() >= constructable.getDifficulty()) {
            constructable.construct();
            getEmployee().changeEnergy(constructable.getDifficulty());
        }

        return constructable.isCompleted();
    }

    @Override
    public int getEnergyCost() {
        return constructable.getDifficulty();
    }
}
