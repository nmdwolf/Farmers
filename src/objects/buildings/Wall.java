package objects.buildings;

import core.*;
import UI.CustomMethods;
import objects.resources.Resource;
import objects.resources.ResourceContainer;
import objects.Obstruction;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class Wall extends IdleBuilding implements Obstruction {

    public final static Award BUILT_AWARD = new Award(CustomMethods.getNewAwardIdentifier(), "Your people are protected.");

    public final static ResourceContainer WALL_COST = new ResourceContainer() {{
        put(Resource.WOOD, -200);
        put(Resource.TIME, 1);
    }};

    public final static int WALL_HEALTH = 500;
    public final static int WALL_SPACE = 5;
    public final static int WALL_SIGHT = 1;
    public final static int WALL_DIFFICULTY = 1;

    public final static int WALL_DEGRADATION_TIME = 50;
    public final static int WALL_DEGRADATION_AMOUNT = 5;

    public Wall(Player player, Cell cell, int cycle) {
        super(player, cell, cycle, WALL_SPACE, WALL_SIGHT, WALL_HEALTH,
                WALL_DEGRADATION_TIME, WALL_DEGRADATION_AMOUNT,
                WALL_COST, WALL_DIFFICULTY);
    }

    @Override
    public String getClassLabel() {
        return "Wall";
    }

    @Override
    public String getToken() {
        return "||";
    }

    @Override
    public @NotNull Optional<BufferedImage> getSprite(boolean max) {
        return Optional.empty();
    }

    @Override
    public Award getConstructionAward() {
        return BUILT_AWARD;
    }
}
