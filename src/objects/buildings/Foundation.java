package objects.buildings;

import UI.CustomMethods;
import core.InternalSettings;
import UI.Sprite;
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

// TODO MERGE WITH BUILDINGS?
public class Foundation<T extends Construction<T>> extends GameObject<T> {

    public final static BufferedImage FOUNDATION_SPRITE_MAX = Sprite.loadSprite("src/img/foundation.png", InternalSettings.SPRITE_SIZE_MAX, InternalSettings.SPRITE_SIZE_MAX).orElseThrow();

    private final T constructable;
    private final ConstructContract<T> contract;
    private int completion;

    public Foundation(T constructable, ConstructContract<T> contract) {
        super(constructable.getTemplate());
        this.contract = contract;
        this.constructable = constructable;
        completion = 0;

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
    public @NotNull Optional<BufferedImage> getSprite() {
        if(constructable.hasVisibleFoundation()) {
            BufferedImage sprite = FOUNDATION_SPRITE_MAX;
            BufferedImage img = new BufferedImage(sprite.getWidth(), sprite.getHeight(), sprite.getType());

            Graphics2D gr = CustomMethods.optimizeGraphics(img.createGraphics());
            gr.drawImage(sprite, 0, 0, null);

            gr.setColor(new Color(getPlayer().getColor().getRed(), getPlayer().getColor().getGreen(), getPlayer().getColor().getBlue(), 128));
            int minSize = Math.min(sprite.getWidth(), sprite.getHeight());

            Area outerDisk = new Area(new Arc2D.Double(sprite.getWidth() == minSize ? 0 : (sprite.getWidth() - minSize) / 2f, sprite.getHeight() == minSize ? 0 : (sprite.getHeight() - minSize) / 2f, minSize, minSize, 0, -(int)(360. * completion / constructable.getConstructionTime()), Arc2D.PIE));

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
        return InternalSettings.BUILDING_TYPE;
    }

    @Override
    public int getSize() {
        return constructable.getSize();
    }

    public void construct() {
        completion++;
        changeHealth(Math.divideExact(constructable.getMaxHealth(), constructable.getConstructionTime()));
    }

    public boolean isComplete() {
        return completion >= constructable.getConstructionTime();
    }
}
