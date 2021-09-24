package items.units;

import static core.Option.*;

import core.*;
import general.CustomMethods;
import general.ResourceContainer;
import items.GameObject;
import items.buildings.House;
import items.buildings.Lumberjack;
import items.upgrade.EvolveUpgrade;
import items.upgrade.Upgrade;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Villager extends Worker {

    public final static BufferedImage sprite = CustomMethods.getSprite("src/img/villager.png", GameConstants.UNIT_SPRITE_SIZE, GameConstants.UNIT_SPRITE_SIZE);
    public final static BufferedImage workingSprite = CustomMethods.getSprite("src/img/villager_working.png", GameConstants.UNIT_SPRITE_SIZE, GameConstants.UNIT_SPRITE_SIZE);

    public final static int VILLAGER_HEALTH = 100;
    public final static int VILLAGER_ENERGY = 5;
    public final static int VILLAGER_SIZE = 1;
    public final static int VILLAGER_SIGHT = 1;

    public final static ResourceContainer VILLAGER_COST = new ResourceContainer(-100, -100, 0, 0, 0, 0);

    public final static int VILLAGER_DEGRADATION_CYCLE = 50;
    public final static int VILLAGER_DEGRADATION_AMOUNT = 2;

    public Villager(Player p, Location loc) {
        super(p, loc, VILLAGER_COST, new HashMap<>() {{
            put(MAX_HEALTH, VILLAGER_HEALTH);
            put(STATUS, GameConstants.IDLE_STATUS);
            put(MAX_ENERGY, VILLAGER_ENERGY);
            put(SIGHT, VILLAGER_SIGHT);
            put(SIZE, VILLAGER_SIZE);
            put(DEGRADATION_AMOUNT, VILLAGER_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE, VILLAGER_DEGRADATION_CYCLE);
            put(HUNT, 5);
            put(DRINK, 5);
            put(LOG, 5);
            put(MASON, 5);
        }});
    }

    @Override
    public String getClassIdentifier() {
        return "Villager";
    }

    @Override
    public String getToken() {
        return "v";
    }

    @Override
    public List<GameObject> getProducts() {
        ArrayList<GameObject> products = new ArrayList<>();
        products.add(new House(getPlayer(), getLocation()));
        products.add(new Lumberjack(getPlayer(), getLocation()));
        return products;
    }

    @Override
    public List<Upgrade> getUpgrades() {
        return null;
    }

    @Override
    public boolean checkStatus(Option option) {
        if(option == ENABLED)
            return true;
        else
            return super.checkStatus(option);
    }

    @Override
    public List<EvolveUpgrade> getEvolutions() {
        return null;
    }

    @Override
    public BufferedImage getSprite() {
        if(getValue(STATUS) != GameConstants.WORKING_STATUS)
            return sprite;
        else
            return workingSprite;
    }
}