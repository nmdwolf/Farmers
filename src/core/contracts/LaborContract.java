package core.contracts;

import core.Cell;
import objects.resources.Resource;
import objects.units.Worker;

/**
 * This Contract type implements the extraction of resources.
 */
public class LaborContract extends Contract {

    private final Resource resource;
    private final Cell cell;

    public LaborContract(Worker employee, Resource r, Cell cell, int cost) throws IllegalArgumentException {
        super(employee, cost);
        if(cell == null)
            throw new IllegalArgumentException("No cell has been assigned.");
        else
            this.cell = cell;
        resource = r;
    }

    public Resource getResource() { return resource; }

    @Override
    public boolean work() {
        int gain = -cell.changeResource(resource, -getEmployee().getYield(resource));
        getEmployee().getPlayer().changeResource(resource, gain);
        getEmployee().step();

        return (cell.getResource(resource) == 0);
    }

    @Override
    public void initialize() {
    }

    @Override
    public void terminate() {

    }

    @Override
    public void abandon() {}
}
