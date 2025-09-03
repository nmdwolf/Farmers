package objects;

import core.OperationsList;
import core.OperationCode;
import core.Status;
import core.contracts.Contract;

import java.util.ArrayList;

/**
 * Signals that a GameObject can "work" or perform operations.
 * This is also used for the graphics modules to generate a working animation (if applicable).
 */
public interface Operational {

    OperationsList getOperations(int cycle, OperationCode code);

    /**
     * Performs work on the list of active contracts if this Unit has sufficient energy.
     */
    void work();

    void addContract(Contract c);

    /**
     * Transfers a contract from an existing owner to this GameObject.
     * @param c contract to transfer
     */
    void transferContract(Contract c);

    ArrayList<Contract> getContracts();

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
}
