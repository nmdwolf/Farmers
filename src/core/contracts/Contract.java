package core.contracts;


import items.units.Worker;

public abstract class Contract {

    private final Worker employee;

    public Contract(Worker party) {
        this.employee = party;
    }

    public Worker getEmployee() {
        return employee;
    }

    /**
     * Work on the contract
     * @return true if contract is completed
     */
    public abstract boolean work();

    /**
     * Returns the energy cost for one call of complete()
     * @return energy cost
     */
    public abstract int getEnergyCost();
}
