package objects;

import core.Cell;
import core.OperationsList;
import core.OperationCode;
import core.Status;
import core.contracts.Contract;
import core.contracts.Logger;
import objects.templates.Template;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;

import static core.InternalSettings.COLD_LEVEL;
import static core.Status.IDLE;

/**
 * Signals that a GameObject can "work" or perform operations.
 * This is also used for the graphics modules to generate a working animation (if applicable).
 */
public abstract class Operational extends GameObject {

    private final ArrayList<Contract> contracts;
    private Logger logger;

    private int energy, maxEnergy;
    private Status status, oldStatus;

    public Operational(Template temp) {
        super(temp);
        contracts = new ArrayList<>();
        logger = new Logger(this);

        this.energy = temp.energy;
        maxEnergy = energy;

        this.status = IDLE;
        this.oldStatus = IDLE;
    }

    /**
     * Gives a list of all operations of the specified type that this object can perform at the current time.
     * @param cycle current cycle
     * @param code type of operations
     * @return list of operations of the specified type
     */
    public OperationsList getOperations(int cycle, OperationCode code) {
        return new OperationsList();
    }

    /**
     * Performs work on the list of active contracts if this object has sufficient energy.
     * TODO implement prioritization of contracts
     */
    public void work() {
        if(!contracts.isEmpty()) {
            setStatus(Status.WORKING);

            for (Iterator<Contract> iterator = contracts.iterator(); iterator.hasNext(); ) {
                Contract c = iterator.next();
                while(getEnergy() >= c.getEnergyCost()) {
                    if (c.work(logger)) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }

        if(contracts.isEmpty())
            setStatus(Status.IDLE);
    };

    /**
     * Adds a contract (compatible with this object's type) to the list of active contracts.
     * TODO This also abandons the current LaborContract(s) if there are any.
     * @param c new contract
     * @throws IllegalArgumentException If the given contract does not have this Worker as assigned employee, an exception is thrown. For existing contracts, the {@code transferContract(Contract c) } method should be used.
     */
    public void addContract(Contract c) {
        if(!c.getEmployee().equals(this))
            throw new IllegalArgumentException("Contract is required to have this Worker as assigned employee.");

        contracts.add(c);
        c.initialize(); // If this fails (e.g. insufficient resources), it will be called again in work() until it succeeds.

        setStatus(Status.WORKING);
    }

    /**
     * Transfers a contract from an existing owner to this object.
     * @param c contract to transfer
     */
    public void transferContract(Contract c){
        c.setEmployee(this);
        addContract(c);
    };

    /**
     * Retrieves the list of active contracts.
     * @return list of active contracts
     */
    public ArrayList<Contract> getContracts() {
        return contracts;
    }

    /**
     * Stops working on all contracts.
     */
    public void seizeActions() {
        contracts.forEach(Contract::abandon);
        contracts.clear();
    }

    /**
     * Gives the currently remaining energy of this object.
     * @return remaining energy
     */
    public int getEnergy() {
        return energy;
    }

    /**
     * Gives the maximum energy this object has.
     * This is the amount of energy this object has at the start of a new cycle (after calling {@code cycle()}.
     * @return maximum energy
     */
    public int getMaxEnergy() {
        return maxEnergy;
    }

    /**
     * Changes the currently available energy.
     * @param amount energy change
     */
    public void increaseEnergy(int amount) {
        energy += amount;
    }

    /**
     * Changes the maximum energy of this object.
     * @param amount energy change
     */
    public void increaseMaxEnergy(int amount) {
        maxEnergy += amount;
        energy += amount;
    }

    /**
     * Returns the current status of the GameObject.
     * @return current status
     */
    public Status getStatus() { return status; }

    /**
     * Returns the old status of the GameObject.
     * @return old status
     */
    public Status getOldStatus() { return oldStatus; }

    /**
     * Changes the current status of the GameObject.
     * @param newStatus new status
     */
    public void setStatus(Status newStatus) {
        oldStatus = status;
        status = newStatus;

        if(oldStatus != newStatus)
            alertChange();
    }

    /**
     * Gives the current {@code Logger} of this object.
     * @return current logger
     */
    public Logger getLogger() {return logger;}

    @Override
    public void cycle(int cycle) {
        super.cycle(cycle);
        if(getStatus() != Status.WALKING)
            work();

        increaseHealth(Math.min(0, getCell().getHeatLevel() - COLD_LEVEL));
    }

    @Override
    public void setCell(@NotNull Cell cell) {
        super.setCell(cell);

        if(getOldStatus() != Status.WALKING)
            seizeActions();
    }

    @Override
    public String toString() {
        return "Type: " + getClassLabel() + "\nPlayer: " + getPlayer().getName() +
                "\nHealth: " + getHealth() + "/" + getMaxHealth() +
                "\nEnergy: " + energy + "/" + maxEnergy + super.toString();
    }

    public void initLogger() {
        logger = new Logger(this);
    }
}
