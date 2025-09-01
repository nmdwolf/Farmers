package objects.resources;

import core.*;
import core.player.Award;
import core.player.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class Farm extends ResourceBuilding {

    public final static int FARM_SIZE = 1;
    public final static int FARM_SIGHT = 1;
    public final static int FARM_HEALTH = 200;
    public final static int FARM_DIFFICULTY = 1;

    public final static int FARM_DEGRADATION_TIME = 1;
    public final static int FARM_DEGRADATION_AMOUNT = 1;

    public final static ResourceContainer FARM_COST = new ResourceContainer() {{
        put(Resource.WOOD, 100);
        put(Resource.WATER, 100);
        put(Resource.TIME, 5);
    }};
    public final static ResourceContainer FARM_GAINS = new ResourceContainer(Resource.FOOD, 10);

    public final static Award BUILT_AWARD = Award.createFreeAward("Food is ours.");

    public Farm(Player p, Cell cell, int cycle) {
        super(p, cell, cycle, FARM_SIZE, FARM_SIGHT, FARM_HEALTH, FARM_COST, FARM_DIFFICULTY,
                FARM_GAINS, FARM_DEGRADATION_TIME, FARM_DEGRADATION_AMOUNT);
    }

    @Override
    public Optional<Award> getConstructionAward() {
        return Optional.of(BUILT_AWARD);
    }

    @Override
    public Resource getResourceType() {
        return Resource.FOOD;
    }

    @Override
    public String getClassLabel() {
        return "Farm";
    }

    @Override
    public String getToken() {
        return "F";
    }
}
