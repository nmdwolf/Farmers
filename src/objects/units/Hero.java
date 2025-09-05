package objects.units;

import UI.CustomMethods;
import com.fasterxml.jackson.annotation.JsonCreator;
import core.*;
import core.OperationsList;
import core.player.Player;
import core.resources.ResourceContainer;
import core.upgrade.Upgrade;
import objects.Aggressive;
import objects.GameObject;
import objects.loadouts.Fighter;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class Hero extends Unit implements Aggressive {

    public final static BufferedImage SPRITE = CustomMethods.getSprite("src/img/hero.png", GameConstants.SPRITE_SIZE, (int)(GameConstants.SPRITE_SIZE / 0.6));
    public final static BufferedImage SPRITE_MAX = CustomMethods.getSprite("src/img/hero.png", GameConstants.SPRITE_SIZE_MAX, (int)(GameConstants.SPRITE_SIZE_MAX / 0.6));

    public final static ResourceContainer HERO_COST = ResourceContainer.EMPTY_CONTAINER;

    public final static int HERO_HEALTH = 300;
    public final static int HERO_ENERGY = 5;
    public final static int HERO_SPACE = 1;
    public final static int HERO_SIGHT = 2;
    public final static int HERO_ANIMATION_DELAY = 1000;
    public final static int HERO_ATTACK = 10;
    public final static int HERO_ATTACK_COST = 1;

    private String name;

    @JsonCreator
    public Hero() {
        super(HERO_ANIMATION_DELAY, HERO_SPACE, HERO_SIGHT, HERO_HEALTH,
                0, 0, 16, HERO_ENERGY, HERO_COST);
        this.name = name;

//        addLoadout(new Fighter<>("Hero", HERO_ATTACK, HERO_ATTACK_COST));
        addLoadout(Fighter.createFighter("Hero"));
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

    @Override
    public int getAttack() {
        return getLoadout(Fighter.class).map(Fighter::getAttack).orElse(0);
    }

    @Override
    public void attack(GameObject obj) {
        getLoadout(Fighter.class).ifPresent(l -> l.attack(obj));
    }

    @Override
    public void changeAttack(int amount) {
        getLoadout(Fighter.class).ifPresent(l -> l.changeAttack(amount));
    }

}
