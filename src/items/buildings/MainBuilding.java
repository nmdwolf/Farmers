package items.buildings;

import static core.Option.*;

import core.*;
import general.CustomMethods;
import general.ResourceContainer;
import items.GameObject;
import items.units.Scout;
import items.units.Villager;
import items.upgrade.EvolveUpgrade;
import items.upgrade.LookoutUpgrade;
import items.upgrade.Upgrade;
import items.upgrade.WellUpgrade;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainBuilding extends ConstructiveBuilding {

    public final static BufferedImage bonfireSprite = CustomMethods.getSprite("src/img/bonfire.png", GameConstants.BUILDING_SPRITE_SIZE, GameConstants.BUILDING_SPRITE_SIZE);
    public final static BufferedImage townSprite = CustomMethods.getSprite("src/img/town.png", GameConstants.BUILDING_SPRITE_SIZE, GameConstants.BUILDING_SPRITE_SIZE);
    public final static BufferedImage castleSprite = CustomMethods.getSprite("src/img/castle.png", GameConstants.BUILDING_SPRITE_SIZE, GameConstants.BUILDING_SPRITE_SIZE);

    public final static ResourceContainer BUILD_RESOURCES = new ResourceContainer() {{
        put(Resource.WOOD, -200);
    }};
    public final static ResourceContainer LEVEL1_RESOURCES = new ResourceContainer() {{
        put(Resource.WOOD, -300);
        put(Resource.STONE, -100);
        put(Resource.WATER, -100);
    }};
    public final static ResourceContainer LEVEL2_RESOURCES = new ResourceContainer() {{
        put(Resource.WOOD, -300);
        put(Resource.STONE, -300);
        put(Resource.WATER, -200);
        put(Resource.IRON, -50);
    }};

    public final static int BASE_HEALTH = 1000;
    public final static int BASE_SPACE = 5;
    public final static int BASE_SIZE = 5;
    public final static int BASE_SIGHT = 1;
    public final static int BASE_HEAL = 5;

    public final static int BASE_DEGRADATION_CYCLE = 50;
    public final static int BASE_DEGRADATION_AMOUNT = 1;

    public final static String TOKEN = "Base";

    public MainBuilding(Player p, Location loc) {
        super(p, loc, BUILD_RESOURCES, new HashMap<>() {{
            put(MAX_HEALTH, BASE_HEALTH);
            put(STATUS, GameConstants.FOUNDATION_KEY);
            put(SPACE, BASE_SPACE);
            put(SIGHT, BASE_SIGHT);
            put(SIZE, BASE_SIZE);
            put(HEAL, BASE_HEAL);
            put(DEGRADATION_AMOUNT, BASE_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE, BASE_DEGRADATION_CYCLE);
        }});
        updateTypes(Type.UPGRADER, Type.EVOLVABLE, Type.HEALER);
    }

    @Override
    public String getClassIdentifier() {
        return switch(getValue(LEVEL)) {
            case 0: yield "Bonfire";
            case 1: yield "Town Center";
            case 2: default: yield "Castle";
        };
    }

    @Override
    public String getToken() {
        return TOKEN;
    }

    @Override
    public List<Upgrade> getUpgrades() {
        ArrayList<Upgrade> upgrades = new ArrayList<>();
        upgrades.add(new LookoutUpgrade(getPlayer()));
        upgrades.add(new WellUpgrade(getPlayer(), this));
        return upgrades;
    }

    @Override
    public List<GameObject> getProducts() {
        ArrayList<GameObject> products = new ArrayList<>();
        products.add(new Villager(getPlayer(), getLocation().add(getValue(CONSTRUCT_X), getValue(CONSTRUCT_Y), 0)));
        products.add(new Scout(getPlayer(), getLocation().add(getValue(CONSTRUCT_X), getValue(CONSTRUCT_Y), 0)));
        return products;
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
        ArrayList<EvolveUpgrade> evolutions = new ArrayList<>(1);
        switch(getValue(LEVEL)) {
            case 0:
                evolutions.add(new EvolveUpgrade<>(MainBuilding.this, LEVEL1_RESOURCES, 0, m -> {
                    m.changeValue(MAX_HEALTH, 200);
                    m.changeValue(SPACE, 2);
                    m.changeValue(CYCLE, 0);
                }));
                break;
            case 1:
                evolutions.add(new EvolveUpgrade<>(MainBuilding.this, LEVEL2_RESOURCES, 0, m -> {
                    m.changeValue(MAX_HEALTH, 300);
                    m.changeValue(SPACE, 3);
                    m.changeValue(CYCLE, 0);
                }));
                break;
        }
        return evolutions;
    }

    @Override
    public BufferedImage getSprite() {
        return switch(getValue(LEVEL)) {
            case 0: yield bonfireSprite;
            case 1: yield townSprite;
            default: yield castleSprite;
        };
    }
}
