package contracts;

import core.Cell;
import resources.Resource;
import units.Worker;

public class LaborContract extends Contract {

    private final Resource resource;
    private final Cell cell;

    public LaborContract(Worker employee, Resource r, Cell cell, int cost) {
        super(employee, cost);
        if(cell == null)
            throw new IllegalStateException("No cell has been assigned.");
        else
            this.cell = cell;
        resource = r;
    }

    public Resource getResource() { return resource; }

    @Override
    public boolean work() {
        int gain = -cell.changeResource(resource, -getEmployee().getYield(resource));
        getEmployee().getPlayer().changeResource(resource, gain);
        return gain == 0;
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
