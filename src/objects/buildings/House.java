package objects.buildings;

import UI.CustomMethods;
import core.player.Award;
import objects.Spacer;
import core.resources.ResourceContainer;
import core.upgrade.*;
import objects.templates.ConstructionTemplate;
import objects.templates.TemplateFactory;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Optional;

import static core.GameConstants.SPRITE_SIZE;
import static core.GameConstants.SPRITE_SIZE_MAX;

public class House extends IdleBuilding implements Upgradable, Spacer {

    public final static BufferedImage SPRITE = CustomMethods.loadSprite("src/img/hut.png", SPRITE_SIZE, SPRITE_SIZE).get();
    public final static BufferedImage SPRITE_MAX = CustomMethods.loadSprite("src/img/hut.png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX).get();
    public final static Award BUILT_AWARD = Award.createFreeAward("You finally gave your people some shelter.");

    public final static String HOUSE_TOKEN = "H";
    public final static int HOUSE_SPACE = 3;

    private int space;

    public House() {
        super((ConstructionTemplate) TemplateFactory.getTemplate("House"));
        this.space = HOUSE_SPACE;
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
    public Optional<Award> getConstructionAward() {
        return Optional.of(BUILT_AWARD);
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
