package objects.buildings;

import core.*;
import UI.CustomMethods;
import UI.OperationsList;
import core.player.Award;
import core.player.Player;
import objects.resources.Resource;
import objects.resources.ResourceContainer;
import objects.Evolvable;
import objects.Spacer;
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

    public final static BufferedImage BONFIRE_SPRITE = CustomMethods.getSprite("src/img/bonfire.png", SPRITE_SIZE, SPRITE_SIZE);
    public final static BufferedImage TOWN_SPRITE = CustomMethods.getSprite("src/img/town.png", SPRITE_SIZE, SPRITE_SIZE);
    public final static BufferedImage CASTLE_SPRITE = CustomMethods.getSprite("src/img/castle.png", SPRITE_SIZE, SPRITE_SIZE);
    public final static BufferedImage BONFIRE_SPRITE_MAX = CustomMethods.getSprite("src/img/bonfire.png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX);
    public final static BufferedImage TOWN_SPRITE_MAX = CustomMethods.getSprite("src/img/town.png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX);
    public final static BufferedImage CASTLE_SPRITE_MAX = CustomMethods.getSprite("src/img/castle.png", SPRITE_SIZE_MAX, SPRITE_SIZE_MAX);
    public final static Award BUILT_AWARD = Award.createFreeAward("A new city has been founded.");

    public final static ResourceContainer BUILD_RESOURCES = new ResourceContainer() {{
        put(Resource.WOOD, -200);
        put(Resource.TIME, 20);
    }};
    public final static ResourceContainer LEVEL1_RESOURCES = new ResourceContainer() {{
        put(Resource.WOOD, -300);
        put(Resource.STONE, -100);
        put(Resource.WATER, -100);
        put(Resource.TIME, 10);
    }};
    public final static ResourceContainer LEVEL2_RESOURCES = new ResourceContainer() {{
        put(Resource.WOOD, -300);
        put(Resource.STONE, -300);
        put(Resource.WATER, -200);
        put(Resource.IRON, -50);
        put(Resource.TIME, 20);
    }};

    public final static int BASE_HEALTH = 1000;
    public final static int BASE_SPACE = 5;
    public final static int BASE_SIZE = 5;
    public final static int BASE_SIGHT = 1;
    public final static int BASE_HEAL = 5;
    public final static int BASE_DIFFICULTY = 1;

    public final static int BASE_DEGRADATION_TIME = 50;
    public final static int BASE_DEGRADATION_AMOUNT = 1;

    public final static int BASE_X = 0;
    public final static int BASE_Y = 0;

    public final static String TOKEN = "Base";

    private int space;

    public TownHall(Player p, Cell cell, int cycle) {
        super(p, cell, cycle, BASE_SIZE, BASE_SIGHT, BASE_HEALTH,
                BASE_DEGRADATION_TIME, BASE_DEGRADATION_AMOUNT, BUILD_RESOURCES, BASE_DIFFICULTY,
                BASE_X, BASE_Y);
        this.space = BASE_SPACE;
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
        operations.put("Villager", () -> {
            Villager v = new Villager(getPlayer(), getCell().fetch(getX(), getY(), 0), cycle);
            if (getPlayer().hasResources(v.getCost())) {
                getPlayer().addObject(v);
                v.construct();
                getPlayer().changeResources(v.getCost().negative());
            }
        });
        operations.put("Scout", () -> { // Construct scout
            Scout sc = new Scout(getPlayer(), getCell().fetch(getX(), getY(), 0), cycle);
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
                        case 1 -> new EvolveUpgrade<>(TownHall.this, LEVEL1_RESOURCES, 0, () -> {
                            changeMaxHealth(200);
                            changeSpaceBoost(2);
                        });
                        case 2 -> new EvolveUpgrade<>(TownHall.this, LEVEL2_RESOURCES, 0, () -> {
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
