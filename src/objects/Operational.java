package objects;

import core.OperationsList;
import core.OperationCode;
import core.contracts.Contract;

import java.util.ArrayList;

/**
 * Signals that a GameObject can "work" or perform operations.
 * This is also used for the graphics modules to generate a working animation (if applicable).
 */
public interface Operational<U extends GameObject<U> & Operational<U>> {

    /**
     * Gives a list of all operations of the specified type that this object can perform at the current time.
     * @param cycle current cycle
     * @param code type of operations
     * @return list of operations of the specified type
     */
    OperationsList getOperations(int cycle, OperationCode code);

    /**
     * Performs work on the list of active contracts if this object has sufficient energy.
     */
    void work();

    /**
     * Adds a contract (compatible with this object's type) to the list of active contracts.
     * @param c new contract
     */
    void addContract(Contract<U> c);

    /**
     * Transfers a contract from an existing owner to this object.
     * @param c contract to transfer
     */
    void transferContract(Contract<U> c);

    /**
     * Retrieves the list of active contracts.
     * @return list of active contracts
     */
    ArrayList<Contract<U>> getContracts();

    /**
     * Stops working on all contracts and stops moving.
     */
    void seizeActions();
}
