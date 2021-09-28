package items.buildings;

import core.*;
import general.ResourceContainer;
import items.GameObject;
import items.upgrade.EvolveUpgrade;
import items.upgrade.LookoutUpgrade;
import items.upgrade.Upgrade;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;

import static core.Option.*;

public class Wall extends Building {

    public final static ResourceContainer WALL_COST = new ResourceContainer() {{
        put(Resource.WOOD, -200);
    }};

    public final static int WALL_HEALTH = 500;
    public final static int WALL_SPACE = 0;
    public final static int WALL_SIZE = 5;
    public final static int WALL_SIGHT = 1;

    public final static int WALL_DEGRADATION_CYCLE = 50;
    public final static int WALL_DEGRADATION_AMOUNT = 5;

    public Wall(Player player, Location loc) {
        super(player, loc, WALL_COST, new HashMap<>() {{
            put(MAX_HEALTH, WALL_HEALTH);
            put(SPACE, WALL_SPACE);
            put(SIGHT, WALL_SIGHT);
            put(SIZE, WALL_SIZE);
            put(DEGRADATION_AMOUNT, WALL_DEGRADATION_AMOUNT);
            put(DEGRADATION_CYCLE, WALL_DEGRADATION_CYCLE);
        }});
        updateTypes(Type.OBSTRUCTION);
    }

    @Override
    public String getClassIdentifier() {
        return "Wall";
    }

    @Override
    public String getToken() {
        return "||";
    }

    @Override
    public BufferedImage getSprite() {
        return null;
    }

    @Override
    public boolean checkStatus(Option option) {
        return option == ENABLED ? getPlayer().hasUpgrade(new LookoutUpgrade(getPlayer())) : super.checkStatus(option);
    }

    @Override
    public List<Upgrade> getUpgrades() {
        return null;
    }

    @Override
    public List<EvolveUpgrade> getEvolutions() {
        return null;
    }

    @Override
    public Award getAward(Option option) {
        return null;
    }
}
