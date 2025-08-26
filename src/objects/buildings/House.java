package objects.buildings;

import core.*;
import UI.CustomMethods;
import core.player.Award;
import core.player.Player;
import objects.Spacer;
import objects.resources.Resource;
import objects.resources.ResourceContainer;
import core.upgrade.*;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Optional;

import static core.GameConstants.SPRITE_SIZE;
import static core.GameConstants.SPRITE_SIZE_MAX;

public class House extends IdleBuilding implements Upgradable, Spacer {

    public final static BufferedImage SPRITE = CustomMethods.getSprite("src/img/hut.png", SPRITE_SIZE, SPRITE_SIZE);
    public final static BufferedImage SPRITE_MAX = CustomMethods.getSprite("src/img/hut.png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX);
    public final static ResourceContainer HOUSE_COST = new ResourceContainer() {{
        put(Resource.WOOD, 50);
        put(Resource.TIME, 5);
    }};
    public final static Award BUILT_AWARD = Award.createFreeAward("You finally gave your people some shelter.");

    public final static int HOUSE_HEALTH = 150;
    public final static int HOUSE_SPACE = 3;
    public final static int HOUSE_SIZE = 1;
    public final static int HOUSE_SIGHT = 1;
    public final static int HOUSE_HEAL = 2;

    public final static int HOUSE_DEGRADATION_TIME = 20;
    public final static int HOUSE_DEGRADATION_AMOUNT = 2;

    public final static String HOUSE_TOKEN = "H";

    private int space;

    public House(Player p, Cell cell, int cycle) {
        super(p, cell, cycle, HOUSE_SIZE, HOUSE_SIGHT, HOUSE_HEALTH,
                HOUSE_DEGRADATION_TIME, HOUSE_DEGRADATION_AMOUNT, HOUSE_COST, 1);

        this.space = HOUSE_SPACE;
    }

    @Override
    public String getClassLabel() {
        return "House";
    }

    @Override
    public String getToken() {
        return HOUSE_TOKEN;
    }

    @Override
    public ArrayList<Upgrade> getUpgrades() {
        ArrayList<Upgrade> upgrades = new ArrayList<>();
        upgrades.add(new LeatherUpgrade(getPlayer()));
        return upgrades;
    }

    @Override
    public @NotNull Optional<BufferedImage> getSprite(boolean max) {
        return Optional.of(max ? SPRITE_MAX : SPRITE);
    }

    @Override
    public @NotNull Award getConstructionAward() {
        return BUILT_AWARD;
    }

    @Override
    public int getSpaceBoost() {
        return space;
    }

    @Override
    public void changeSpaceBoost(int amount) {
        space += amount;
    }
}
