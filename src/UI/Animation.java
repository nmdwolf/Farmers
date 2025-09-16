package UI;

import core.Pair;
import core.contracts.*;
import objects.Animated;
import objects.GameObject;
import objects.Operational;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Objects;

public class Animation extends ArrayDeque<Pair<BufferedImage, String>> {

    private final Animated<?> object;
    private final BufferedImage image;
    private final int length;
    private int code;

    public Animation(Animated<?> obj, int numOfFrames) {
        super(numOfFrames * obj.getLogger().size());

        object = obj;
        image = Objects.requireNonNull(((GameObject<?>)object).getSprite(true).get());

        Logger logger = obj.getLogger();
        length = numOfFrames;
        int framesPerLog = numOfFrames / logger.size();

        StringBuilder description = new StringBuilder(((GameObject<?>)object).getClassLabel() + " worked on " + logger.size() + " contract" + ((logger.size() > 1) ? 's' : "") + ".");

        while(logger.size() > 0) {
            Logger.Log log = logger.pop();
            code = log.type;
            description.append("\n").append(log.description);
            for (int i = 0; i < framesPerLog; i++)
                addLast(new Pair<>(generateFrame(i, framesPerLog), description.toString()));
        }
    }

    /**
     * Yields the total number of frames in this animation.
     * @return total number of frames
     */
    public int length() {
        return length;
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

    private BufferedImage generateFrame(int step, int length) {
        if(code == Logger.Log.CONSTRUCT)
            return CustomMethods.rotateImage(image, -2 * Math.PI * step / length);
        else if(code == Logger.Log.LABOUR)
            return CustomMethods.rotateImage(image, 2 * Math.PI * step / length);
        else if(code == Logger.Log.ATTACK) {
            double shift = image.getWidth() / 2d;
            double scale = Math.cos(2 * Math.PI * step / length);
            AffineTransform af = new AffineTransform();
            af.translate(shift, 0);
            af.scale(scale, 1);
            af.translate(-shift, 0);

            BufferedImage frame = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
            Graphics2D gr = CustomMethods.optimizeGraphics(frame.createGraphics());
            gr.drawRenderedImage(image, af);
            gr.setTransform(new AffineTransform());
            gr.dispose();
            return frame;
        }

        throw new IllegalArgumentException("Animation code is not implemented or not recognized.");
    }
}
