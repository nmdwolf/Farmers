package core.contracts;


import objects.units.Worker;

public abstract class Contract {

    private final Worker employee;
    private final int cost;

    public Contract(Worker party, int energy) {
        employee = party;
        cost = energy;
    }

    public Worker getEmployee() {
        return employee;
    }

    /**
     * Sets up the contract and deals with thins such as costs.
     */
    public abstract void initialize();

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
    public abstract boolean work();

    /**
     * Returns the energy cost for one call of work()
     * @return energy cost
     */
    public int getEnergyCost() { return cost; }
}
