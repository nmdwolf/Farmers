package core.contracts;

import core.Cell;
import objects.units.Worker;

/**
 * This Contract type implements the extraction of resources.
 */
public class LaborContract extends Contract<Worker> {

    private final String resource;
    private final Cell cell;

    public LaborContract(Worker employee, String r, Cell cell) throws IllegalArgumentException {
        super(employee, employee.getGatherCost());
        if(cell == null)
            throw new IllegalArgumentException("No cell has been assigned.");
        else
            this.cell = cell;
        resource = r;
    }

    public String getResource() { return resource; }

    @Override
    public boolean work(Logger logger) {
        if(super.work(logger)) {
            int amount = getEmployee().getYield(resource);
            int gain = -cell.changeResource(resource, -amount);
            getEmployee().getPlayer().changeResource(resource, gain);
            getEmployee().step();

            boolean finished = (cell.getResource(resource) == 0);
            logger.logLabour(resource, finished);
            return finished;
        }
        return false;
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
