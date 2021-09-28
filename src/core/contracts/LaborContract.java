package core.contracts;

import core.Cell;
import core.Resource;
import items.units.Worker;

public class LaborContract extends Contract {

    private final Resource resource;
    private final int energyCost;
    private Cell cell;

    public LaborContract(Worker employee, Resource r, int cost) {
        super(employee);
        energyCost = cost;
        resource = r;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public Resource getResource() { return resource; }

    @Override
    public int getEnergyCost() {
        return energyCost;
    }

    @Override
    public boolean work() {
        if(cell == null)
            throw new IllegalStateException("No cell has been assigned.");

        int gain = -cell.changeResource(resource, -Math.min(cell.getResource(resource), getEmployee().getValue(resource.operation)));
        getEmployee().getPlayer().changeResource(resource, gain);
        return gain == 0;
    }
}
