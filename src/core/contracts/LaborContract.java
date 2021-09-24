package core.contracts;

import core.Cell;
import core.GameConstants;
import core.Resource;
import items.units.Worker;

import static core.GameConstants.WORKING_STATUS;
import static core.Option.STATUS;

public class LaborContract extends Contract {

    private final Resource resource;
    private final int energyCost;
    private final Cell station;

    public LaborContract(Worker p, Resource r, int c, Cell s) {
        super(p, GameConstants.LABOR_KEY);
        energyCost = c;
        resource = r;
        station = s;
    }

    public Resource getResource() { return resource; }

    @Override
    public int getEnergyCost() {
        return energyCost;
    }

    @Override
    public int getType() {
        return GameConstants.LABOR_KEY;
    }

    @Override
    public boolean work() {
        int amount = getParty().getValue(resource.operation);
        getParty().getPlayer().changeResource(resource, Math.min(station.getResource(resource), amount));
        station.changeResource(resource, -Math.min(station.getResource(resource), amount));
        return station.getResource(resource) == 0;
    }
}
