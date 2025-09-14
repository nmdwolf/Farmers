package objects.buildings;

import UI.CustomMethods;
import core.contracts.ConstructContract;
import objects.Construction;
import objects.GameObject;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Optional;

import static core.GameConstants.*;

// TODO MERGE WITH BUILDINGS?
public class Foundation<T extends Construction> extends GameObject {

    public final static BufferedImage FOUNDATION_SPRITE = CustomMethods.loadSprite("src/img/foundation.png", SPRITE_SIZE, SPRITE_SIZE).orElseThrow();
    public final static BufferedImage FOUNDATION_SPRITE_MAX = CustomMethods.loadSprite("src/img/foundation.png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX).orElseThrow();

    private final T constructable;
    private final ConstructContract<T> contract;

    public Foundation(T constructable, ConstructContract<T> contract) {
        super(constructable.getTemplate());
        this.contract = contract;
        this.constructable = constructable;

        changeHealth(-(getMaxHealth() - 1));
    }

    public ConstructContract<T> getContract() {
        return contract;
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
        if(constructable.hasVisibleFoundation()) {
            BufferedImage sprite = max ? FOUNDATION_SPRITE_MAX : FOUNDATION_SPRITE;
            BufferedImage img = new BufferedImage(sprite.getWidth(), sprite.getHeight(), sprite.getType());

            Graphics2D gr = CustomMethods.optimizeGraphics(img.createGraphics());
            gr.drawImage(sprite, 0, 0, null);

            gr.setColor(new Color(getPlayer().getColor().getRed(), getPlayer().getColor().getGreen(), getPlayer().getColor().getBlue(), 128));
            int minSize = Math.min(sprite.getWidth(), sprite.getHeight());

            Area outerDisk = new Area(new Arc2D.Double(sprite.getWidth() == minSize ? 0 : (sprite.getWidth() - minSize) / 2f, sprite.getHeight() == minSize ? 0 : (sprite.getHeight() - minSize) / 2f, minSize, minSize, 0, -(int)(360. * constructable.getCompletionLevel() / constructable.getConstructionTime()), Arc2D.PIE));

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
    public void cycle(int cycle) {}

    @Override
    public void degrade(int cycle) {}

    @Override
    public String toString() {
        return "Foundation of [" + constructable.getClassLabel() + "]" + "\n" +
                "Health: " + getHealth() + "/" + getMaxHealth();
    }

    @Override
    public int getType() {
        return BUILDING_TYPE;
    }
}
