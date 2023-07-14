package items.units;

import core.*;
import general.OperationsList;
import general.ResourceContainer;
import items.Evolvable;
import items.buildings.House;
import items.upgrade.EvolveUpgrade;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import static core.Option.*;

public class Scout extends Unit implements Evolvable {

    public final static int SCOUT_HEALTH = 100;
    public final static int SCOUT_ENERGY = 10;
    public final static int SCOUT_SIZE = 2;
    public final static int SCOUT_SIGHT = 2;
    public final static int SCOUT_ANIMATION = 300;

    public final static ResourceContainer SCOUT_COST = new ResourceContainer(){{
        put(Resource.FOOD, -100);
        put(Resource.WATER, -200);
        put(Resource.TIME, 0);
    }};
    public final static ResourceContainer LEVEL1_COST = new ResourceContainer(){{
        put(Resource.FOOD, -200);
        put(Resource.WATER, -200);
        put(Resource.TIME, 10);
    }};

    public final static int SCOUT_DEGRADATION_CYCLE = 50;
    public final static int SCOUT_DEGRADATION_AMOUNT = 2;

    public Scout(Player p, Cell cell) {
        super(p, cell, SCOUT_SIZE, SCOUT_COST, new HashMap<>() {{
            put(MAX_HEALTH, SCOUT_HEALTH);
            put(STATUS, GameConstants.IDLE_STATUS);
            put(MAX_ENERGY, SCOUT_ENERGY);
            put(SIGHT, SCOUT_SIGHT);
            put(SIZE, SCOUT_SIZE);
            put(DEGRADATION_AMOUNT, SCOUT_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE, SCOUT_DEGRADATION_CYCLE);
            put(ANIMATION, SCOUT_ANIMATION);
        }});
    }

    @Override
    public String getClassLabel() {
        return "Scout";
    }

    @Override
    public String getToken() {
        return "s";
    }

    @Override
    public boolean checkStatus(Option option) {
        if(option == ENABLED)
            return getPlayer().hasEnabled(House.BUILT_AWARD);
        else
            return super.checkStatus(option);
    }

    @Override
    public OperationsList getEvolutions() {
        OperationsList operations =  new OperationsList();

        operations.put("Evolve", () -> {
            EvolveUpgrade<Scout> ev =  new EvolveUpgrade<>(this, LEVEL1_COST, 0, () -> {
                this.changeValue(SIGHT, 1);
                this.changeValue(MAX_ENERGY, 5);
                this.changeValue(MAX_HEALTH, 20);
                this.changeValue(DEGRADATION_AMOUNT, 1);
                this.changeValue(CYCLE, 0);
            });
            if(ev.isPossible())
                ev.upgrade();
        });

        return operations;
    }

    @Override
    public Award getAward(Option option) {
        return null;
    }

    @Override
    public BufferedImage getSprite() {
        return null;
    }
}
