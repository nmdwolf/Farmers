package objects;

public interface Animated extends Operational {

    void step();

    /**
     * Current step in the working cycle.
     * @return current working stage
     */
    int getCurrentStep();

    /**
     * The total number of steps in a working cycle.
     * (This should be a multiple of 4.)
     * @return working cycle length
     */
    int getCycleLength();

    int getAnimationDelay();
}
