package core.contracts;

import core.Option;
import items.buildings.Building;
import items.units.Worker;

public class BuildContract extends Contract{

    private final Building building;

    public BuildContract(Worker employee, Building b) {
        super(employee);
        building = b;
    }

    @Override
    public boolean work() {
        building.perform(Option.CONSTRUCT);
        return building.checkStatus(Option.CONSTRUCT);
    }

    @Override
    public int getEnergyCost() {
        return 1;
    }
}
