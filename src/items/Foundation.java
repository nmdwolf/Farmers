package items;

import core.*;
import general.CustomMethods;
import general.OperationsList;
import general.ResourceContainer;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class Foundation<T extends Constructable> extends GameObject {

    public final static BufferedImage FOUNDATION_SPRITE = CustomMethods.getSprite("src/img/foundation.png", GameConstants.BUILDING_SPRITE_SIZE, GameConstants.BUILDING_SPRITE_SIZE);

    private final boolean visible;
    private boolean built;
    private final OperationsList operations;
    private final T constructable;

    public Foundation(Player p, T constructable, boolean visible) {
        super(p, constructable.getCell(), constructable.getSize(), new HashMap<>(){{
            put(Option.SIGHT, 0);
            put(Option.SIZE, 0);
            put(Option.MAX_HEALTH, 0);
            put(Option.DEGRADATION_AMOUNT, 0);
            put(Option.DEGRADATION_CYCLE, 0);
            put(Option.CONSTRUCT, 0);
        }});
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
    public BufferedImage getSprite() {
        if(visible)
            return FOUNDATION_SPRITE;
        else
            return null;
    }

    @Override
    public Award getAward(Option option) {
        return null;
    }

    @Override
    public OperationsList getOperations(Option... options) {
        return operations;
    }

    @Override
    public void perform(Option option) {
        if(option == Option.CONSTRUCT)
            built = true;
        else
            super.perform(option);
    }

    @Override
    public boolean checkStatus(Option option) {
        return (option == Option.DESTROY) && built;
    }
}
