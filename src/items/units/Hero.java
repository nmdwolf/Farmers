package items.units;

import core.*;
import general.CustomMethods;
import general.OperationsList;
import general.ResourceContainer;
import items.upgrade.LookoutUpgrade;
import items.upgrade.Upgrade;
import items.upgrade.WellUpgrade;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

import static core.Option.*;

public class Hero extends Unit{

    private final String name;

    public final static ResourceContainer HERO_COST = ResourceContainer.EMPTY_CONTAINER;

    public final static int HERO_HEALTH = 300;
    public final static int HERO_ENERGY = 5;
    public final static int HERO_SIZE = 1;
    public final static int HERO_SIGHT = 2;
    public final static int HERO_ANIMATION = 1000;

    public Hero(Player p, Cell cell, String name) {
        super(p, cell, HERO_SIZE, HERO_COST, new HashMap<>() {{
            put(MAX_HEALTH, HERO_HEALTH);
            put(STATUS, GameConstants.IDLE_STATUS);
            put(MAX_ENERGY, HERO_ENERGY);
            put(SIGHT, HERO_SIGHT);
            put(SIZE, HERO_SIZE);
            put(DEGRADATION_AMOUNT, 0);
            put(DEGRADATION_CYCLE, 0);
            put(ANIMATION, HERO_ANIMATION);
        }});
        this.name = name;
    }

    @Override
    public String getClassLabel() {
        return "Hero " + name;
    }

    @Override
    public String getToken() {
        return "H";
    }

    @Override
    public BufferedImage getSprite() {
        return null;
    }

    @Override
    public Award getAward(Option option) {
        return null;
    }

    @Override
    public OperationsList getOperations(Option... options) {
        OperationsList operations = super.getOperations(options);
        for(Option option : options) {
            if (option == UPGRADE) {
                List<Upgrade> civUpgrades = getPlayer().getCivilization().getUpgrades();
                for(Upgrade u : civUpgrades)
                    operations.putUpgrade(u.toString(), u, getPlayer());
            }
        }
        return operations;
    }
}
