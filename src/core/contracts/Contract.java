package core.contracts;


import items.units.Worker;

public abstract class Contract {

    private final int type;
    private Worker party;

    public Contract(Worker party, int type) {
        this.party = party;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public Worker getParty() {
        return party;
    }

    /**
     * Work on the contract
     * @return true if contract is fully completed
     */
    public abstract boolean work();

    /**
     * Returns the energy cost for one call of complete()
     * @return energy cost
     */
    public abstract int getEnergyCost();
}
