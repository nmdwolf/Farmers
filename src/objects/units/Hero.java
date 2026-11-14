package objects.units;

import core.InternalSettings;
import UI.Sprite;
import core.*;
import core.OperationsList;
import core.player.Player;
import core.upgrade.Upgrade;
import objects.templates.TemplateFactory;
import objects.templates.UnitTemplate;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class Hero extends Unit {

    public final static BufferedImage SPRITE_MAX = Sprite.loadSprite("src/img/hero.png", InternalSettings.SPRITE_SIZE_MAX, (int)(InternalSettings.SPRITE_SIZE_MAX / 0.6)).orElseThrow();

    private String name;

    public Hero() {
        super((UnitTemplate) TemplateFactory.getTemplate("Hero"));
    }

    @Override
    public void initialize(Player player, int cycle) {
        super.initialize(player, cycle);
        for (Upgrade u : getPlayer().getCivilization().getUpgrades())
            getUpgrades().add(u);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getClassLabel() {
        return "Hero " + name;
    }

    @Override
    public String getToken() {
        return name;
    }

    @Override
    public @NotNull Optional<BufferedImage> getSprite() {
        return Optional.of(SPRITE_MAX);
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        return OperationsList.EMPTY_LIST;
    }
}
