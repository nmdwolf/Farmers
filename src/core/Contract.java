package core;

import items.GameObject;

public abstract class Contract {

    private final int type;
    private GameObject party;

    public Contract(GameObject p, int t) {
        party = p;
        type = t;
    }

    public int getType() {
        return type;
    }

    public GameObject getParty() {
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
