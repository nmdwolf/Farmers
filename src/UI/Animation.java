package UI;

import core.Pair;
import core.contracts.*;
import objects.Animated;
import objects.GameObject;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Objects;

public class Animation extends ArrayDeque<Pair<BufferedImage, String>> {

    private final Animated object;
    private final BufferedImage image;
    private int code;

    public Animation(Animated obj, int numOfFrames) {
        super(numOfFrames * obj.getLogger().size());

        object = obj;
        image = Objects.requireNonNull(object.getSprite().orElseThrow());

        Logger logger = obj.getLogger();

        // determine how many frames are needed to handle the logs
        int framesPerLog = numOfFrames / logger.size();

        // construct a string representation of the actions performed by this object (using the Logger class)
        StringBuilder description = new StringBuilder(object.getClassLabel() + " worked on " + logger.size() + " contract" + ((logger.size() > 1) ? 's' : "") + ".");

        // As long as there are logs remaining, add them to the list of actions
        while(logger.size() > 0) {
            Logger.Log log = logger.pop();
            code = log.type;
            description.append("\n").append(log.description);
            for (int i = 0; i < framesPerLog; i++)
                addLast(new Pair<>(generateFrame(i, framesPerLog), description.toString()));
        }
    }

    private BufferedImage generateFrame(int step, int length) {
        if(code == Logger.Log.CONSTRUCT)
            return CustomMethods.rotateImageCentered(image, -2 * Math.PI * step / length);
        else if(code == Logger.Log.LABOUR)
            return CustomMethods.rotateImageCentered(image, 2 * Math.PI * step / length);
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
