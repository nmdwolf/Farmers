package core.contracts;

import core.Cell;
import core.GameConstants;
import core.Resource;
import items.units.Worker;

public class LaborContract extends Contract {

    private final Resource resource;
    private final int energyCost, amount;
    private final Cell station;

    public LaborContract(Worker p, Resource r, int c, int a, Cell s) {
        super(p, GameConstants.LABOR_KEY);
        amount = a;
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
        getParty().getPlayer().changeResource(resource, Math.min(station.getResource(resource), amount));
        station.changeResource(resource, -Math.min(station.getResource(resource), amount));
        return station.getResource(resource) == 0;
    }
}
