package objects.buildings;

import core.player.Award;
import core.resources.ResourceContainer;

import java.util.Optional;

public class Farm extends ResourceBuilding {

    public final static int FARM_SIZE = 1;
    public final static int FARM_SIGHT = 1;
    public final static int FARM_HEALTH = 200;
    public final static int FARM_DIFFICULTY = 1;

    public final static int FARM_DEGRADATION_TIME = 1;
    public final static int FARM_DEGRADATION_AMOUNT = 1;

    public final static ResourceContainer FARM_COST = new ResourceContainer(new String[]{"Wood", "Water", "Time"}, new int[]{100, 100, 1});
    public final static ResourceContainer FARM_GAINS = new ResourceContainer("Food", 10);

    public final static Award BUILT_AWARD = Award.createFreeAward("Food is ours.");

    public Farm() {
        super(FARM_SIZE, FARM_SIGHT, FARM_HEALTH, FARM_COST, FARM_DIFFICULTY,
                FARM_GAINS, FARM_DEGRADATION_TIME, FARM_DEGRADATION_AMOUNT);
    }

    @Override
    public Optional<Award> getConstructionAward() {
        return Optional.of(BUILT_AWARD);
    }

    @Override
    public String getResourceType() {
        return "Food";
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
