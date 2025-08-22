package objects;

import core.Cell;
import core.player.Player;
import core.Status;
import objects.resources.ResourceContainer;

import static core.Status.IDLE;

/**
 * Signals that a GameObject can have a "Working" status.
 * This is used for the graphics to generate a working animation.
 */
public abstract class Operational extends Constructable {

    private Status status, oldStatus;
    private int step;
    private final int cycleLength;
    private GameObject target;

    public Operational(Player player, Cell cell, int cycle, int space, int sight, int health,
                       int degradeTime, int degradeAmount, int cycleLength,
                       ResourceContainer cost, int difficulty, boolean hasVisibileFoundation) {
        super(player, cell, cycle, space, sight, health, degradeTime, degradeAmount,
                cost, difficulty, hasVisibileFoundation);

        this.status = IDLE;
        this.oldStatus = IDLE;
        this.target = null;

        this.step = 0;

        if(cycleLength == 0)
            throw new IllegalArgumentException("Cycle length has to be nonzero.");
        else
            this.cycleLength = cycleLength;
    }

    /**
     * Current step in the working cycle.
     * @return current working stage
     */
    public int getCurrentStep() { return step; }

    /**
     * The total number of steps in a working cycle.
     * (Should be a multiple of 4.)
     * @return working cycle length
     */
    public int getCycleLength() { return cycleLength; }

    public void step() { step = (++step) % cycleLength; }

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
    }

    public void setTarget(GameObject newTarget) {
        target = newTarget;
    }

    public GameObject getTarget() {
        return target;
    }
}
