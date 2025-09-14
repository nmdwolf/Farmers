package objects;

public interface Animated<U extends GameObject<U> & Operational<U>> extends Operational<U> {

    @Deprecated
    void step();

    /**
     * Current step in the working cycle.
     * @return current working stage
     */
    @Deprecated
    int getCurrentStep();

    /**
     * The total number of steps in a working cycle.
     * (This should be a multiple of 4.)
     * @return working cycle length
     */
    @Deprecated
    int getCycleLength();

    /**
     * Gives this object's "move speed". Used by {@code Motion}'s for animation purposes.
     * @return move speed
     */
    int getAnimationDelay();
}
