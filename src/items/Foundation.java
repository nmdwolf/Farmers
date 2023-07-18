package items;

import core.*;
import general.CustomMethods;
import general.OperationsList;

import java.awt.image.BufferedImage;

// MERGE WITH BUILDINGS?
public class Foundation<T extends Constructable> extends GameObject {

    public final static BufferedImage FOUNDATION_SPRITE = CustomMethods.getSprite("src/img/foundation.png", GameConstants.BUILDING_SPRITE_SIZE, GameConstants.BUILDING_SPRITE_SIZE);

    public final static int FOUNDATION_HEALTH = 1;

    private final boolean visible;
    private final OperationsList operations;

    public Foundation(Player p, T constructable, boolean visible, int cycle) {
        super(p, constructable.getCell(), cycle, constructable.getSpace(), constructable.getSight(), FOUNDATION_HEALTH, 0, 0);
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
    public BufferedImage getSprite() {
        if(visible)
            return FOUNDATION_SPRITE;
        else
            return null;
    }

    @Override
    public OperationsList getOperations(int cycle) {
        return operations;
    }

    @Override
    public void cycle(int cycle) {}

    @Override
    public void degrade(int cycle) {}
}
