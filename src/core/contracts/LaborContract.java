package core.contracts;

import core.Cell;
import core.Resource;
import items.units.Worker;

public class LaborContract extends Contract {

    private final Resource resource;
    private final int energyCost;
    private final Cell cell;

    public LaborContract(Worker employee, Resource r, Cell cell, int cost) {
        super(employee, -1);
        this.cell = cell;
        energyCost = cost;
        resource = r;
    }

    public Resource getResource() { return resource; }

    @Override
    public int getEnergyCost() {
        return energyCost;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void terminate() {

    }

    @Override
    public boolean work() {
        if(cell == null)
            throw new IllegalStateException("No cell has been assigned.");

        int gain = -cell.changeResource(resource, -Math.min(cell.getResource(resource), getEmployee().getYield(resource)));
        getEmployee().getPlayer().changeResource(resource, gain);
        return gain == 0;
    }
}
