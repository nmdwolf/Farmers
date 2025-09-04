package objects.buildings;

import core.*;
import UI.CustomMethods;
import core.player.Award;
import core.player.Player;
import core.resources.Resource;
import core.resources.ResourceContainer;
import objects.Obstruction;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;

import static core.GameConstants.SPRITE_SIZE;
import static core.GameConstants.SPRITE_SIZE_MAX;

// TODO Implement direction
public class Wall extends IdleBuilding implements Obstruction, Directional {

    public final static Award BUILT_AWARD = Award.createFreeAward("Your people are protected.");
    public final static BufferedImage SPRITE = CustomMethods.getSprite("src/img/Wall.png", SPRITE_SIZE, SPRITE_SIZE);
    public final static BufferedImage SPRITE_MAX = CustomMethods.getSprite("src/img/Wall.png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX);

    public final static ResourceContainer WALL_COST = new ResourceContainer() {{
        put(Resource.WOOD, 0);
        put(Resource.TIME, 1);
    }};

    public final static int WALL_HEALTH = 500;
    public final static int WALL_SPACE = 5;
    public final static int WALL_SIGHT = 1;
    public final static int WALL_DIFFICULTY = 1;

    public final static int WALL_DEGRADATION_TIME = 50;
    public final static int WALL_DEGRADATION_AMOUNT = 5;

    public Wall() {
        super(WALL_SPACE, WALL_SIGHT, WALL_HEALTH,
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
        return Optional.of(max ? SPRITE_MAX : SPRITE);
    }

    @Override
    public Optional<Award> getConstructionAward() {
        return Optional.of(BUILT_AWARD);
    }

    @Override
    public @NotNull Direction getDirection() {
        return null;
    }
}
