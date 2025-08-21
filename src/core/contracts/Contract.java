package core.contracts;


import objects.units.Worker;

public abstract class Contract {

    private final Worker employee;
    private final int cost;
    private boolean isStarted;

    public Contract(Worker party, int energy) {
        employee = party;
        cost = energy;
        isStarted = false;
    }

    public Worker getEmployee() {
        return employee;
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
