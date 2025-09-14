package core.contracts;

import objects.GameObject;
import objects.Operational;

public abstract class Contract<T extends GameObject<T> & Operational<T>> {

    private T employee;
    private final int energyCost;
    private boolean isStarted, idle;
    private int workCount;

    public Contract(T employee, int energyCost) {
        this.employee = employee;
        this.energyCost = energyCost;
        isStarted = false;
        idle = true;
        workCount = 0;
    }

    public T getEmployee() {
        return employee;
    }

    public void setEmployee(T worker) {
        employee = worker;
        idle = (worker == null);
    }

    public boolean isIdle() {
        return idle;
    }

    /**
     * Sets up the contract and deals with things such as costs.
     */
    public void initialize() {
        isStarted = true;
        idle = false;
    }

    public boolean isStarted() { return isStarted; }

    /**
     * Terminates the contract and handles.
     */
    public abstract void terminate();

    /**
     * Terminates the contract in case it was not completed.
     */
    public void abandon() { idle = true; }

    /**
     * Work on the contract.
     * @return true if contract is completed
     */
    public boolean work(Logger logger) {
        if(!isStarted)
            initialize();

        if (isStarted && employee.getEnergy() >= energyCost) {
            employee.changeEnergy(-energyCost);
            workCount++;
        }

        return isStarted;
    }

    /**
     * Returns the amount of times this contract has been worked on and resets the counter.
     * @return work count
     */
    public int popWorkCount() {
        int temp = workCount;
        workCount = 0;
        return temp;
    }

    /**
     * Returns the energy cost for one call of work()
     * @return energy cost
     */
    public int getEnergyCost() { return energyCost; }
}
