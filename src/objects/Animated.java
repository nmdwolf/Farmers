package objects;

import objects.templates.Template;

public abstract class Animated extends Operational {

    public Animated(Template temp) {
        super(temp);
    }

    /**
     * Gives this object's "move speed". Used by {@code Motion}'s for animation purposes.
     * @return move speed
     */
    public abstract int getAnimationDelay();
}
