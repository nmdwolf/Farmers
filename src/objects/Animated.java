package objects;

public interface Animated extends Energetic {

    @Deprecated
    void nextFrame();

    /**
     * Current step in the working cycle.
     * @return current working stage
     */
    @Deprecated
    int getCurrentFrame();

    /**
     * The total number of steps in a working cycle.
     * (This should be a multiple of 4.)
     * @return working cycle length
     */
    @Deprecated
    int getAnimationLength();

    /**
     * Gives this object's "move speed". Used by {@code Motion}'s for animation purposes.
     * @return move speed
     */
    int getAnimationDelay();
}
