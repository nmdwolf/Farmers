package objects;

import core.OperationsList;
import core.OperationCode;
import core.Status;
import core.contracts.Contract;
import core.contracts.Logger;

import java.util.ArrayList;

/**
 * Signals that a GameObject can "work" or perform operations.
 * This is also used for the graphics modules to generate a working animation (if applicable).
 */
public interface Operational<U extends GameObject & Operational<U>> {

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

    /**
     * Gives the currently remaining energy of this object.
     * @return remaining energy
     */
    int getEnergy();

    /**
     * Gives the maximum energy this object has.
     * This is the amount of energy this object has at the start of a new cycle (after calling {@code cycle()}.
     * @return maximum energy
     */
    int getMaxEnergy();

    /**
     * Changes the currently available energy.
     * @param amount energy change
     */
    void changeEnergy(int amount);

    /**
     * Changes the maximum energy of this object.
     * @param amount energy change
     */
    void changeMaxEnergy(int amount);

    /**
     * Returns the current status of the GameObject.
     * @return current status
     */
    Status getStatus();

    /**
     * Returns the old status of the GameObject.
     * @return old status
     */
    Status getOldStatus();

    /**
     * Changes the current status of the GameObject.
     * @param newStatus new status
     */
    void setStatus(Status newStatus);

    /**
     * Sets a new {@code Logger} for this object.
     */
    void initLogger();

    /**
     * Gives the current {@code Logger} of this object.
     * @return current logger
     */
    Logger getLogger();
}
