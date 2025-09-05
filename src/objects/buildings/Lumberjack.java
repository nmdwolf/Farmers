package objects.buildings;

import core.player.Award;
import core.resources.ResourceContainer;
import objects.Booster;
import objects.GameObject;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class Lumberjack extends IdleBuilding implements Booster {

    public final static ResourceContainer LUMBERJACK_COST = new ResourceContainer(new String[]{"Food", "Water", "Time"}, new int[]{100, 50, 1});
    public final static Award BUILT_AWARD = Award.createFreeAward("You figured out how to chop wood.");

    public final static int LUMBERJACK_HEALTH = 250;
    public final static int LUMBERJACK_SPACE = 1;
    public final static int LUMBERJACK_SIGHT = 1;

    public final static int LUMBERJACK_DEGRADATION_TIME = 30;
    public final static int LUMBERJACK_DEGRADATION_AMOUNT = 2;

    public Lumberjack() {
        super(LUMBERJACK_SPACE, LUMBERJACK_SIGHT, LUMBERJACK_HEALTH,
                LUMBERJACK_DEGRADATION_TIME, LUMBERJACK_DEGRADATION_AMOUNT, LUMBERJACK_COST, 1);
    }

    @Override
    public String getClassLabel() {
        return "Lumberjack";
    }

    @Override
    public String getToken() {
        return "L";
    }

    @Override
    public @NotNull Optional<BufferedImage> getSprite(boolean max) {
        return Optional.empty();
    }

    @Override
    public int getBoostRadius() { return 2; }

    @Override
    public int getBoostAmount(GameObject obj, String res) {
        if(res.equals("Wood"))
            return 2;
        else
            return 0;
    }

    @Override
    public Optional<Award> getConstructionAward() {
        return Optional.of(BUILT_AWARD);
    }
}
