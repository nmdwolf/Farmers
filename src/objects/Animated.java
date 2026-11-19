package objects;

import objects.templates.Template;

public abstract class Animated extends Operational {

    private final int movementDelay, animationDelay;

    public Animated(Template temp) {
        super(temp);
        this.movementDelay = temp.animationDelay;
        this.animationDelay = temp.animationDelay;
    }

    /**
     * The number of milliseconds between movement frames.
     * @return movement delay
     */
    public int getMovementDelay() { return movementDelay; }

    /**
     * The number of milliseconds between animation frames.
     * @return animation delay
     */
    public int getAnimationDelay() { return animationDelay; }
}
