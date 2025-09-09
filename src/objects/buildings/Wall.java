package objects.buildings;

import UI.CustomMethods;
import core.player.Award;
import objects.Obstruction;
import objects.templates.ConstructionTemplate;
import objects.templates.TemplateFactory;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;

import static core.GameConstants.SPRITE_SIZE;
import static core.GameConstants.SPRITE_SIZE_MAX;

// TODO Implement direction
public class Wall extends IdleBuilding implements Obstruction, Directional {

    public final static Award BUILT_AWARD = Award.createFreeAward("Your people are protected.");
    public final static BufferedImage SPRITE = CustomMethods.loadSprite("src/img/Wall.png", SPRITE_SIZE, SPRITE_SIZE).get();
    public final static BufferedImage SPRITE_MAX = CustomMethods.loadSprite("src/img/Wall.png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX).get();

    public Wall() {
        super((ConstructionTemplate) TemplateFactory.getTemplate("Wall"));
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
