package objects.buildings;

import core.*;
import UI.CustomMethods;
import UI.OperationsList;
import objects.Constructable;
import objects.GameObject;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Optional;

import static core.GameConstants.SPRITE_SIZE;
import static core.GameConstants.SPRITE_SIZE_MAX;

// MERGE WITH BUILDINGS?
public class Foundation<T extends Constructable> extends GameObject {

    public final static BufferedImage FOUNDATION_SPRITE = CustomMethods.getSprite("src/img/foundation.png", SPRITE_SIZE, SPRITE_SIZE);
    public final static BufferedImage FOUNDATION_SPRITE_MAX = CustomMethods.getSprite("src/img/foundation.png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX);

    public final static int FOUNDATION_HEALTH = 1;

    private final boolean visible;
    private final OperationsList operations;
    private final T constructable;

    public Foundation(Player p, T constructable, boolean visible, int cycle) {
        super(p, constructable.getCell(), cycle, constructable.getSpace(), constructable.getSight(), FOUNDATION_HEALTH, 0, 0);
        this.constructable = constructable;
        this.visible = visible;
        this.operations = new OperationsList();
    }

    @Override
    public String getClassLabel() {
        return "Foundation";
    }

    @Override
    public String getToken() {
        return "!";
    }

    @Override
    public @NotNull Optional<BufferedImage> getSprite(boolean max) {
        if(visible) {
            BufferedImage sprite = max ? FOUNDATION_SPRITE_MAX : FOUNDATION_SPRITE;
            BufferedImage img = new BufferedImage(sprite.getWidth(), sprite.getHeight(), sprite.getType());

            Graphics2D gr = CustomMethods.optimizeGraphics(img.createGraphics());
            gr.drawImage(sprite, 0, 0, null);

            gr.setColor(new Color(getPlayer().getColor().getRed(), getPlayer().getColor().getGreen(), getPlayer().getColor().getBlue(), 128));
            int minSize = Math.min(sprite.getWidth(), sprite.getHeight());

            Area outerDisk = new Area(new Arc2D.Double(sprite.getWidth() == minSize ? 0 : (sprite.getWidth() - minSize) / 2f, sprite.getHeight() == minSize ? 0 : (sprite.getHeight() - minSize) / 2f, minSize, minSize, 0, -(int)(360. * constructable.getCompletion() / constructable.getRequirement()), Arc2D.PIE));

            int innerSize = minSize / 2;
            Area innerDisk = new Area(new Ellipse2D.Double(sprite.getWidth() == minSize ? minSize / 4f : (sprite.getWidth() - minSize) / 2f + minSize / 4f, sprite.getHeight() == minSize ? minSize / 4f : (sprite.getHeight() - minSize) / 2f + minSize / 4f, innerSize, innerSize));

            outerDisk.subtract(innerDisk);
            gr.fill(outerDisk);

            gr.dispose();
            return Optional.of(img);
        }
        else
            return Optional.empty();
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        return operations;
    }

    @Override
    public void cycle(int cycle) {}

    @Override
    public void degrade(int cycle) {}
}
