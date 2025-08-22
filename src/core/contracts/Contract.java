package core.contracts;


import objects.units.Worker;

public abstract class Contract {

    private Worker employee;
    private final int cost;
    private boolean isStarted, idle;

    public Contract(Worker employee, int energy) {
        this.employee = employee;
        cost = energy;
        isStarted = false;
        idle = false;
    }

    public Worker getEmployee() {
        return employee;
    }

    public void setEmployee(Worker worker) {
        employee = worker;
        idle = (worker == null);
    }

    public boolean isIdle() {
        return idle;
    }

    /**
     * Sets up the contract and deals with things such as costs.
     */
    public void initialize() {
        isStarted = true;
    }

    public boolean isStarted() { return isStarted; }

    /**
     * Terminates the contract and handles.
     */
    public abstract void terminate();

    /**
     * Terminates the contract in case it was not completed.
     */
    public abstract void abandon();

    /**
     * Work on the contract.
     * @return true if contract is completed
     */
    public boolean work() {
        if(!isStarted)
            initialize();
        return isStarted;
    }

    /**
     * Returns the energy cost for one call of work()
     * @return energy cost
     */
    public int getEnergyCost() { return cost; }
}
