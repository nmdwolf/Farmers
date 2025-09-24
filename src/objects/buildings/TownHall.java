package objects.buildings;

import UI.Sprite;
import core.*;
import core.OperationsList;
import core.player.Player;
import core.resources.ResourceContainer;
import objects.Evolvable;
import objects.Spacer;
import objects.templates.ConstructionTemplate;
import objects.templates.TemplateFactory;
import objects.units.Scout;
import objects.units.Villager;
import core.upgrade.EvolveUpgrade;
import core.upgrade.LookoutUpgrade;
import core.upgrade.WellUpgrade;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class TownHall extends ConstructiveBuilding<TownHall> implements Spacer, Evolvable {

    public final static ResourceContainer LEVEL1_RESOURCES = new ResourceContainer(new String[]{"Wood", "Stone", "Water", "Time"}, new int[]{300, 100, 100, 10});
    public final static ResourceContainer LEVEL2_RESOURCES = new ResourceContainer(new String[]{"Wood", "Stone", "Water", "Iron", "Time"}, new int[]{300, 300, 200, 50, 20});

    public final static int BASE_X = 0;
    public final static int BASE_Y = 0;

    public final static String TOKEN = "Base";

    private int level;

    public TownHall() {
        super((ConstructionTemplate) TemplateFactory.getTemplate("Townhall"), BASE_X, BASE_Y);
        level = 1;
    }

    @Override
    public void initialize(Player player, int cycle) {
        super.initialize(player, cycle);
        getUpgrades().add(new LookoutUpgrade());
        getUpgrades().add(new WellUpgrade(this));
    }

    @Override
    public String getClassLabel() {
        return switch (getLevel()) {
            case 0 -> "Bonfire";
            case 1 -> "Town Center";
            default -> "Castle";
        };
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void increaseLevel() {
        level++;
    }

    @Override
    public String getToken() {
        return TOKEN;
    }

    @Override
    public @NotNull Optional<BufferedImage> getSprite() {
        return switch (getLevel()) {
            case 1 -> Sprite.loadSprite("src/img/bonfire.png", Sprite.getSpriteSize(), Sprite.getSpriteSize());
            case 2 -> Sprite.loadSprite("src/img/town.png", Sprite.getSpriteSize(), Sprite.getSpriteSize());
            default -> Sprite.loadSprite("src/img/castle.png", Sprite.getSpriteSize(), Sprite.getSpriteSize());
        };
    }

    @Override
    public OperationsList getConstructions(int cycle) {
        OperationsList operations = new OperationsList();
        operations.put("Villager", _ -> {
            Villager v = new Villager();
            if (getPlayer().hasResources(v.getCost())) {
                getPlayer().addObject(v, getCell());
                getPlayer().changeResources(v.getCost().negative());
            }
        });
        operations.put("Scout", _ -> { // Construct scout
            Scout sc = new Scout();
            if (getPlayer().hasResources(sc.getCost())) {
                getPlayer().addObject(sc, getCell());
                getPlayer().changeResources(sc.getCost().negative());
            }
        });
        return operations;
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        OperationsList operations = new OperationsList();
        if(code == OperationCode.EVOLVE) {
            operations.put("Evolve",
                    _ -> {
                        switch (getLevel()) {
                            case 1 -> new EvolveUpgrade<>(TownHall.this, LEVEL1_RESOURCES, 0, _ -> {
                                changeMaxHealth(200);
                                changeSpaceBoost(2);
                            }).upgrade(getPlayer());
                            case 2 -> new EvolveUpgrade<>(TownHall.this, LEVEL2_RESOURCES, 0, _ -> {
                                changeMaxHealth(300);
                                changeSpaceBoost(3);
                            }).upgrade(getPlayer());
                            default -> {}
                        }
                    });
        } if(code == OperationCode.CONSTRUCTION) {
            operations = getConstructions(cycle);
        } else
            operations = super.getOperations(cycle, code);
        return operations;
    }

    @Override
    public void cycle(int cycle) {

    }

    @Override
    public void changeSpaceBoost(int amount) {
        getLoadout(objects.loadouts.Spacer.class).ifPresent(spacer -> spacer.changeSpaceBoost(amount));
    }

    @Override
    public int getSpaceBoost() {
        return getLoadout(objects.loadouts.Spacer.class).map(objects.loadouts.Spacer::getSpaceBoost).orElseThrow();
    }
}
