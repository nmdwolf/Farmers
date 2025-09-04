package objects.units;

import core.*;
import core.contracts.Contract;
import core.player.Player;
import objects.Animated;
import objects.Construction;
import core.resources.ResourceContainer;
import core.Status;
import objects.loadouts.Fighter;

import java.util.ArrayList;
import java.util.Iterator;

import static core.GameConstants.COLD_LEVEL;
import static core.GameConstants.UNIT_TYPE;
import static core.Status.IDLE;

public abstract class Unit extends Construction implements Animated {

    public final static int UNIT_DIFFICULTY = 1;

    private int energy, maxEnergy, step;
    private final int animationDelay;
    private Status status, oldStatus;
    private final int cycleLength;
    private final ArrayList<Contract> contracts;

    public Unit(int animationDelay, int size, int sight, int health,
                int degradeTime, int degradeAmount, int cycleLength,
                int energy, ResourceContainer cost) {
        super(size, sight, health, degradeTime, degradeAmount, cost, UNIT_DIFFICULTY, false);

        this.animationDelay = animationDelay;
        this.energy = energy;
        maxEnergy = energy;
        step = 0;

        this.status = IDLE;
        this.oldStatus = IDLE;

        contracts = new ArrayList<>();

        if(cycleLength == 0)
            throw new IllegalArgumentException("Cycle length has to be nonzero.");
        else
            this.cycleLength = cycleLength;
    }

    @Override
    public void cycle(int cycle) {
        super.cycle(cycle);
        energy = maxEnergy;
        changeHealth(Math.min(0, getCell().getHeatLevel() - COLD_LEVEL));

        if(getStatus() != Status.WALKING)
            work();
    }

    public int getEnergy() {
        return energy;
    }

    public void changeEnergy(int amount) {
        energy += amount;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public void changeMaxEnergy(int amount) {
        maxEnergy += amount;
        energy += amount;
    }

    public int getAnimationDelay() { return animationDelay; }

    @Override
    public int getType() {
        return UNIT_TYPE;
    }

    /**
     * Current step in the working cycle.
     * @return current working stage
     */
    @Override
    public int getCurrentStep() { return step; }

    /**
     * The total number of steps in a working cycle.
     * (Should be a multiple of 4.)
     * @return working cycle length
     */
    @Override
    public int getCycleLength() { return cycleLength; }

    @Override
    public void step() { step = (++step) % cycleLength; }

    /**
     * Returns the current status of the GameObject.
     * @return current status
     */
    @Override
    public Status getStatus() { return status; }

    /**
     * Returns the old status of the GameObject.
     * @return old status
     */
    @Override
    public Status getOldStatus() { return oldStatus; }

    /**
     * Changes the current status of the GameObject.
     * @param newStatus new status
     */
    @Override
    public void setStatus(Status newStatus) {
        oldStatus = status;
        status = newStatus;
    }

    /**
     * Performs work on the list of active contracts if this Unit has sufficient energy.
     * TODO implement prioritization of contracts
     */
    @Override
    public void work() {
        if(!contracts.isEmpty()) {
            setStatus(Status.WORKING);

            for (Iterator<Contract> iterator = contracts.iterator(); iterator.hasNext(); ) {
                Contract c = iterator.next();
                if (getEnergy() >= c.getEnergyCost()) {
                    changeEnergy(-c.getEnergyCost());
                    if (c.work())
                        iterator.remove();
                }
            }
        }

        if(contracts.isEmpty())
            setStatus(Status.IDLE);
    }

    @Override
    public void setCell(Cell cell) {
        super.setCell(cell);

        if(getOldStatus() != Status.WALKING)
            seizeActions();
    }

    /**
     * Adds a contract to this Worker's active contracts.
     * This also abandons the current LaborContract(s) if there are any.
     * @param c new contract
     * @throws IllegalArgumentException If the given contract does not have this Worker as assigned employee, an exception is thrown. For existing contracts, the {@code transferContract(Contract c) } method should be used.
     */
    @Override
    public void addContract(Contract c) throws IllegalArgumentException {
        if(!c.getEmployee().equals(this))
            throw new IllegalArgumentException("Contract is required to have this Worker as assigned employee.");

        contracts.add(c);
        c.initialize(); // If this fails (e.g. insufficient resources), it will be called again in work() until it succeeds.
        setStatus(Status.WORKING);
    }

    /**
     * Intended to be used in the same way as {@code addContract(Contract c)} with the sole difference that this
     * method first sets the employee of the given contract to be this Worker.
     * @param c new contract
     */
    @Override
    public void transferContract(Contract c) {
        c.setEmployee(this);
        addContract(c);
    }

    /**
     * Handles contract removal on move, fight, ...
     * Abandons {@code LaborContracts} and unsets employee of {@code ConstructContracts}.
     */
    @Override
    public void seizeActions() {
        contracts.forEach(Contract::abandon);
        contracts.clear();
        setStatus(Status.IDLE);
    }

    @Override
    public ArrayList<Contract> getContracts() { return contracts; }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Type: " + getClassLabel() + "\nPlayer: " + getPlayer().getName() +
                "\nHealth: " + getHealth() + "/" + getMaxHealth() +
                "\nEnergy: " + energy + "/" + maxEnergy + "\n\n");

        getLoadout(Fighter.class).ifPresent(s::append);

        return s.toString();
    }
}
