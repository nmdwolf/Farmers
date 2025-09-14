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

    OperationsList getOperations(int cycle, OperationCode code);

    /**
     * Performs work on the list of active contracts if this Unit has sufficient energy.
     */
    void work();

    void addContract(Contract<U> c);

    /**
     * Transfers a contract from an existing owner to this GameObject.
     * @param c contract to transfer
     */
    void transferContract(Contract<U> c);

    ArrayList<Contract<U>> getContracts();

    void seizeActions();

    int getEnergy();

    int getMaxEnergy();

    void changeEnergy(int amount);

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
