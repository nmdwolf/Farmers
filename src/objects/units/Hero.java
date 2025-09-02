package objects.units;

import UI.CustomMethods;
import core.*;
import UI.OperationsList;
import core.player.Player;
import core.resources.ResourceContainer;
import core.upgrade.Upgrade;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class Hero extends Unit{

    public final static BufferedImage SPRITE = CustomMethods.getSprite("src/img/hero.png", GameConstants.SPRITE_SIZE, (int)(GameConstants.SPRITE_SIZE / 0.6));
    public final static BufferedImage SPRITE_MAX = CustomMethods.getSprite("src/img/hero.png", GameConstants.SPRITE_SIZE_MAX, (int)(GameConstants.SPRITE_SIZE_MAX / 0.6));

    public final static ResourceContainer HERO_COST = ResourceContainer.EMPTY_CONTAINER;

    public final static int HERO_HEALTH = 300;
    public final static int HERO_ENERGY = 5;
    public final static int HERO_SPACE = 1;
    public final static int HERO_SIGHT = 2;
    public final static int HERO_ANIMATION = 1000;

    private final String name;

    public Hero(Player p, Cell cell, int cycle, String name) {
        super(p, cell, cycle, HERO_ANIMATION, HERO_SPACE, HERO_SIGHT, HERO_HEALTH,
                0, 0, 1, HERO_ENERGY, HERO_COST);
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
    public @NotNull Optional<BufferedImage> getSprite(boolean max) {
        return Optional.of(max ? SPRITE_MAX : SPRITE);
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        OperationsList operations = new OperationsList();
        if(code == OperationCode.UPGRADE) {
            for (Upgrade u : getPlayer().getCivilization().getUpgrades())
                operations.putUpgrade(u.toString(), u);
        }
        return operations;
    }

}
