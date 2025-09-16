package objects;

import core.Status;
import core.contracts.Logger;

public interface Energetic<T extends GameObject<T> & Energetic<T>> extends Operational<T> {

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
