package items.buildings;

import static core.Option.*;

import core.*;
import general.CustomMethods;
import general.OperationsList;
import general.ResourceContainer;
import items.Evolvable;
import items.Spacer;
import items.units.Scout;
import items.units.Villager;
import items.upgrade.EvolveUpgrade;
import items.upgrade.LookoutUpgrade;
import items.upgrade.WellUpgrade;

import java.awt.image.BufferedImage;
import java.util.HashMap;

public class MainBuilding extends ConstructiveBuilding implements Spacer, Evolvable {

    public final static BufferedImage bonfireSprite = CustomMethods.getSprite("src/img/bonfire.png", GameConstants.BUILDING_SPRITE_SIZE, GameConstants.BUILDING_SPRITE_SIZE);
    public final static BufferedImage townSprite = CustomMethods.getSprite("src/img/town.png", GameConstants.BUILDING_SPRITE_SIZE, GameConstants.BUILDING_SPRITE_SIZE);
    public final static BufferedImage castleSprite = CustomMethods.getSprite("src/img/castle.png", GameConstants.BUILDING_SPRITE_SIZE, GameConstants.BUILDING_SPRITE_SIZE);

    public final static ResourceContainer BUILD_RESOURCES = new ResourceContainer() {{
        put(Resource.WOOD, -200);
        put(Resource.TIME, 20);
    }};
    public final static ResourceContainer LEVEL1_RESOURCES = new ResourceContainer() {{
        put(Resource.WOOD, -300);
        put(Resource.STONE, -100);
        put(Resource.WATER, -100);
        put(Resource.TIME, 10);
    }};
    public final static ResourceContainer LEVEL2_RESOURCES = new ResourceContainer() {{
        put(Resource.WOOD, -300);
        put(Resource.STONE, -300);
        put(Resource.WATER, -200);
        put(Resource.IRON, -50);
        put(Resource.TIME, 20);
    }};

    public final static int BASE_HEALTH = 1000;
    public final static int BASE_SPACE = 5;
    public final static int BASE_SIZE = 5;
    public final static int BASE_SIGHT = 1;
    public final static int BASE_HEAL = 5;
    public final static int BASE_DIFFICULTY = 1;

    public final static int BASE_DEGRADATION_CYCLE = 50;
    public final static int BASE_DEGRADATION_AMOUNT = 1;

    public final static String TOKEN = "Base";

    public MainBuilding(Player p, Cell cell) {
        super(p, cell, BASE_SIZE, BUILD_RESOURCES, BASE_DIFFICULTY, new HashMap<>() {{
            put(MAX_HEALTH, BASE_HEALTH);
            put(SPACE, BASE_SPACE);
            put(SIGHT, BASE_SIGHT);
            put(SIZE, BASE_SIZE);
            put(HEAL, BASE_HEAL);
            put(CONSTRUCT, 5);
            put(DEGRADATION_AMOUNT, BASE_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE, BASE_DEGRADATION_CYCLE);
        }});
    }

    @Override
    public String getClassLabel() {
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
    public boolean checkStatus(Option option) {
        if(option == ENABLED)
            return true;
        else
            return super.checkStatus(option);
    }

    @Override
    public BufferedImage getSprite() {
        return switch(getValue(LEVEL)) {
            case 0: yield bonfireSprite;
            case 1: yield townSprite;
            default: yield castleSprite;
        };
    }

    @Override
    public Award getAward(Option option) {
        return null;
    }

    @Override
    public OperationsList getOperations(Option... options) {
        OperationsList operations = super.getOperations(options);
        for(Option option : options) {
            if(option == CONSTRUCT) {
                operations.put("Villager", () -> {
                    Villager v = new Villager(getPlayer(), getCell().fetch(getValue(CONSTRUCT_X), getValue(CONSTRUCT_Y), 0));
                    if (getPlayer().hasResources(v.getResources(CONSTRUCT)))
                        v.perform(CONSTRUCT);
                }); // Construct villager
                operations.put("Scout", () -> {
                    Scout sc = new Scout(getPlayer(), getCell().fetch(getValue(CONSTRUCT_X), getValue(CONSTRUCT_Y), 0));
                    if (getPlayer().hasResources(sc.getResources(CONSTRUCT)))
                        sc.perform(CONSTRUCT);
                }); // Construct scout
            } else if (option == UPGRADE) {
                LookoutUpgrade lookout = new LookoutUpgrade(getPlayer());
                operations.putUpgrade(lookout.toString(), lookout, getPlayer());

                WellUpgrade well = new WellUpgrade(this);
                operations.putUpgrade(well.toString(), well, getPlayer());
            }
        }
        return operations;
    }

    @Override
    public OperationsList getEvolutions() {
        OperationsList operations = new OperationsList();
        operations.put("Evolve", () -> {
            EvolveUpgrade<MainBuilding> ev = switch(getValue(LEVEL)) {
                case 0: yield new EvolveUpgrade<>(MainBuilding.this, LEVEL1_RESOURCES, 0, () -> {
                    this.changeValue(MAX_HEALTH, 200);
                    this.changeValue(SPACE, 2);
                    this.changeValue(CYCLE, 0);
                });
                case 1: yield new EvolveUpgrade<>(MainBuilding.this, LEVEL2_RESOURCES, 0, () -> {
                    this.changeValue(MAX_HEALTH, 300);
                    this.changeValue(SPACE, 3);
                    this.changeValue(CYCLE, 0);
                });
                default: yield null;
            };

            if(ev != null && ev.isPossible())
                ev.upgrade();
        });
        return operations;
    }

    @Override
    public int getSpace() {
        return BASE_SPACE;
    }
}
