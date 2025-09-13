package UI;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;

public class Animation extends ArrayDeque<BufferedImage> {

    private final BufferedImage image;
    private final double length;

    public Animation(BufferedImage background, int numOfFrames) {
        super(numOfFrames);
        image = background;
        length = numOfFrames;

        for(int i = 0; i < numOfFrames; i++)
            addLast(CustomMethods.rotateImage(image, -2 * Math.PI * i / length));
    }

    public int length() {
        return (int)length;
    }

    /**
     * Returns the number of remaining frames.
     * Equivalent to {@code size()}.
     * @return number of remaining frames
     */
    public int remaining() {
        return this.size();
    }

    /**
     * Returns whether this animation has remaining frames.
     * Equivalent to {@code !isEmpty()}.
     * @return remaining frames or not
     */
    public boolean hasRemaining() {
        return !this.isEmpty();
    }
}
