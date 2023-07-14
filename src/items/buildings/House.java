package items.buildings;

import core.*;
import general.CustomMethods;
import general.OperationsList;
import general.ResourceContainer;
import items.upgrade.*;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import static core.Option.*;

public class House extends Building {

    public final static BufferedImage sprite = CustomMethods.getSprite("src/img/hut.png", GameConstants.BUILDING_SPRITE_SIZE, GameConstants.BUILDING_SPRITE_SIZE);
    public final static ResourceContainer HOUSE_COST = new ResourceContainer() {{
        put(Resource.WOOD, -50);
        put(Resource.TIME, 5);
    }};
    public final static Award BUILT_AWARD = new Award(CustomMethods.getNewAwardIdentifier(), "You finally gave your people some shelter.");

    public final static int HOUSE_HEALTH = 150;
    public final static int HOUSE_SPACE = 3;
    public final static int HOUSE_SIZE = 1;
    public final static int HOUSE_SIGHT = 1;
    public final static int HOUSE_HEAL = 2;

    public final static int HOUSE_DEGRADATION_CYCLE = 20;
    public final static int HOUSE_DEGRADATION_AMOUNT = 2;

    public final static String HOUSE_TOKEN = "H";

    public House(Player p, Cell cell) {
        super(p, cell, HOUSE_SIZE, HOUSE_COST, 1, new HashMap<>() {{
            put(MAX_HEALTH, HOUSE_HEALTH);
            put(SPACE, HOUSE_SPACE);
            put(SIGHT, HOUSE_SIGHT);
            put(SIZE, HOUSE_SIZE);
            put(HEAL, HOUSE_HEAL);
            put(CONSTRUCT, 1);
            put(DEGRADATION_AMOUNT, HOUSE_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE, HOUSE_DEGRADATION_CYCLE);
        }});
    }

    @Override
    public boolean checkStatus(Option option) {
        if(option == ENABLED)
            return true;
        else
            return super.checkStatus(option);
    }

    @Override
    public String getClassLabel() {
        return "House";
    }

    @Override
    public String getToken() {
        return HOUSE_TOKEN;
    }

    @Override
    public OperationsList getOperations(Option... options) {
        OperationsList operations = super.getOperations(options);
        for(Option option : options) {
            if (option == UPGRADE) {
                LeatherUpgrade leather = new LeatherUpgrade(getPlayer());
                operations.putUpgrade("Lookout", leather, getPlayer());
            }
        }
        return operations;
    }

    @Override
    public BufferedImage getSprite() {
        return sprite;
    }

    @Override
    public Award getAward(Option option) {
        return option == CONSTRUCT ? BUILT_AWARD : null;
    }
}
