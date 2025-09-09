package objects.buildings;

import core.*;
import UI.CustomMethods;
import core.OperationsList;
import core.player.Award;
import core.resources.ResourceContainer;
import objects.Evolvable;
import objects.Spacer;
import objects.templates.ConstructionTemplate;
import objects.templates.TemplateFactory;
import objects.templates.UnitTemplate;
import objects.units.Scout;
import objects.units.Villager;
import core.upgrade.EvolveUpgrade;
import core.upgrade.LookoutUpgrade;
import core.upgrade.WellUpgrade;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;

import static core.GameConstants.SPRITE_SIZE;
import static core.GameConstants.SPRITE_SIZE_MAX;

public class TownHall extends ConstructiveBuilding implements Spacer, Evolvable {

    public final static BufferedImage BONFIRE_SPRITE = CustomMethods.loadSprite("src/img/bonfire.png", SPRITE_SIZE, SPRITE_SIZE).get();
    public final static BufferedImage TOWN_SPRITE = CustomMethods.loadSprite("src/img/town.png", SPRITE_SIZE, SPRITE_SIZE).get();
    public final static BufferedImage CASTLE_SPRITE = CustomMethods.loadSprite("src/img/castle.png", SPRITE_SIZE, SPRITE_SIZE).get();
    public final static BufferedImage BONFIRE_SPRITE_MAX = CustomMethods.loadSprite("src/img/bonfire.png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX).get();
    public final static BufferedImage TOWN_SPRITE_MAX = CustomMethods.loadSprite("src/img/town.png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX).get();
    public final static BufferedImage CASTLE_SPRITE_MAX = CustomMethods.loadSprite("src/img/castle.png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX).get();
    public final static Award BUILT_AWARD = Award.createFreeAward("A new city has been founded.");

    public final static ResourceContainer LEVEL1_RESOURCES = new ResourceContainer(new String[]{"Wood", "Stone", "Water", "Time"}, new int[]{300, 100, 100, 10});
    public final static ResourceContainer LEVEL2_RESOURCES = new ResourceContainer(new String[]{"Wood", "Stone", "Water", "Iron", "Time"}, new int[]{300, 300, 200, 50, 20});

    public final static int BASE_X = 0;
    public final static int BASE_Y = 0;
    public final static int BASE_SPACE = 5;

    public final static String TOKEN = "Base";

    private int space;
    private int level;

    public TownHall() {
        super((ConstructionTemplate) TemplateFactory.getTemplate("Townhall"), BASE_X, BASE_Y);
        this.space = BASE_SPACE;
        level = 1;
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
    public @NotNull Optional<BufferedImage> getSprite(boolean max) {
        return switch (getLevel()) {
            case 0 -> Optional.of(max ? BONFIRE_SPRITE_MAX : BONFIRE_SPRITE);
            case 1 -> Optional.of(max ? TOWN_SPRITE_MAX : TOWN_SPRITE);
            default -> Optional.of(max ? CASTLE_SPRITE_MAX : CASTLE_SPRITE);
        };
    }

    @Override
    public OperationsList getConstructions(int cycle) {
        OperationsList operations = new OperationsList();
        operations.put("Villager", _ -> {
            Villager v = new Villager();
            if (getPlayer().hasResources(v.getCost())) {
                v.initialize(getPlayer(), getCell().fetch(getX(), getY(), 0), cycle);
                getPlayer().addObject(v);
                v.construct();
                getPlayer().changeResources(v.getCost().negative());
            }
        });
        operations.put("Scout", _ -> { // Construct scout
            Scout sc = new Scout();
            sc.initialize(getPlayer(), getCell().fetch(getX(), getY(), 0), cycle);
            if (getPlayer().hasResources(sc.getCost())) {
                getPlayer().addObject(sc);
                sc.construct();
            }
        });
        return operations;
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        OperationsList operations = new OperationsList();
        if(code == OperationCode.UPGRADE) {
            LookoutUpgrade lookout = new LookoutUpgrade(getPlayer());
            operations.putUpgrade(lookout.toString(), lookout);

            WellUpgrade well = new WellUpgrade(this);
            operations.putUpgrade(well.toString(), well);
        } else if(code == OperationCode.EVOLVE) {
            operations.putUpgrade("Evolve",
                    switch (getLevel()) {
                        case 1 -> new EvolveUpgrade<>(TownHall.this, LEVEL1_RESOURCES, 0, _ -> {
                            changeMaxHealth(200);
                            changeSpaceBoost(2);
                        });
                        case 2 -> new EvolveUpgrade<>(TownHall.this, LEVEL2_RESOURCES, 0, _ -> {
                            changeMaxHealth(300);
                            changeSpaceBoost(3);
                        });
                        default -> null;
                    });
        } else
            operations = super.getOperations(cycle, code);
        return operations;
    }

    @Override
    public Optional<Award> getConstructionAward() {
        return Optional.of(BUILT_AWARD);
    }

    @Override
    public void cycle(int cycle) {

    }

    @Override
    public void changeSpaceBoost(int amount) {
        space += amount;
    }

    @Override
    public int getSpaceBoost() {
        return space;
    }
}
