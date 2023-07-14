package core.contracts;


import items.units.Worker;

public abstract class Contract {

    private final Worker employee;
    private int completion, required;

    public Contract(Worker party, int required) {
        this.employee = party;
        this.completion = 0;
        this.required = required;
    }

    public Worker getEmployee() {
        return employee;
    }

    /**
     * Sets up the contract and deals with thins such as costs.
     */
    public abstract void initialize();

    /**
     * Terminates the contract and handles
     */
    public abstract void terminate();

    /**
     * Work on the contract
     * @return true if contract is completed
     */
    public boolean work() {
        completion += 1;
        if(completion == required)
            return true;
        return false;
    }

    /**
     * Returns the energy cost for one call of work()
     * @return energy cost
     */
    public abstract int getEnergyCost();
}
