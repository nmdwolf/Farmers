package objects.units;

import UI.CustomMethods;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.*;
import core.OperationsList;
import core.resources.ResourceContainer;
import core.upgrade.Upgrade;
import objects.Aggressive;
import objects.GameObject;
import objects.loadouts.Fighter;
import objects.templates.TemplateFactory;
import objects.templates.UnitTemplate;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Hero extends Unit implements Aggressive {

    public final static BufferedImage SPRITE = CustomMethods.loadSprite("src/img/hero.png", GameConstants.SPRITE_SIZE, (int)(GameConstants.SPRITE_SIZE / 0.6)).get();
    public final static BufferedImage SPRITE_MAX = CustomMethods.loadSprite("src/img/hero.png", GameConstants.SPRITE_SIZE_MAX, (int)(GameConstants.SPRITE_SIZE_MAX / 0.6)).get();

    private String name;

    public Hero() {
        super((UnitTemplate) TemplateFactory.getTemplate("Hero"));
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
    public int getAttackCost() {
        return getLoadout(Fighter.class).map(Fighter::getAttackCost).orElse(0);
    }

    @Override
    public int getRange() {
        return getLoadout(Fighter.class).map(Fighter::getRange).orElse(0);
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
