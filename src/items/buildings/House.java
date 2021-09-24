package items.buildings;

import core.*;
import general.CustomMethods;
import general.ResourceContainer;
import items.GameObject;
import items.upgrade.EvolveUpgrade;
import items.upgrade.LeatherUpgrade;
import items.upgrade.Upgrade;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static core.Option.*;

public class House extends Building {

    public final static BufferedImage sprite = CustomMethods.getSprite("src/img/hut.png", GameConstants.BUILDING_SPRITE_SIZE, GameConstants.BUILDING_SPRITE_SIZE);

    public final static ResourceContainer HOUSE_COST = new ResourceContainer() {{
        put(Resource.WOOD, -50);
    }};

    public final static int HOUSE_HEALTH = 150;
    public final static int HOUSE_SPACE = 3;
    public final static int HOUSE_SIZE = 1;
    public final static int HOUSE_SIGHT = 1;
    public final static int HOUSE_HEAL = 2;

    public final static int HOUSE_DEGRADATION_CYCLE = 20;
    public final static int HOUSE_DEGRADATION_AMOUNT = 2;

    public final static String HOUSE_TOKEN = "H";

    public House(Player p, Location loc) {
        super(p, loc, HOUSE_COST, new HashMap<>() {{
            put(MAX_HEALTH, HOUSE_HEALTH);
            put(STATUS, GameConstants.FOUNDATION_KEY);
            put(SPACE, HOUSE_SPACE);
            put(SIGHT, HOUSE_SIGHT);
            put(SIZE, HOUSE_SIZE);
            put(HEAL, HOUSE_HEAL);
            put(DEGRADATION_AMOUNT, HOUSE_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE, HOUSE_DEGRADATION_CYCLE);
        }});
        updateTypes(Type.EVOLVABLE, Type.HEALER);
    }

    @Override
    public boolean checkStatus(Option option) {
        if(option == ENABLED)
            return true;
        else
            return super.checkStatus(option);
    }

    @Override
    public String getClassIdentifier() {
        return "House";
    }

    @Override
    public String getToken() {
        return HOUSE_TOKEN;
    }

    @Override
    public List<Upgrade> getUpgrades() {
        ArrayList<Upgrade> upgrades = new ArrayList<>();
        upgrades.add(new LeatherUpgrade(getPlayer()));
        return upgrades;
    }

    @Override
    public List<EvolveUpgrade> getEvolutions() {
        return null;
    }

    @Override
    public List<GameObject> getProducts() {
        return null;
    }

    @Override
    public BufferedImage getSprite() {
        return sprite;
    }
}
