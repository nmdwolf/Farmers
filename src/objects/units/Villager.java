package objects.units;

import core.*;
import core.contracts.ConstructContract;
import UI.CustomMethods;
import core.OperationsList;
import core.contracts.Contract;
import core.player.Award;
import objects.Constructor;
import core.Status;
import objects.buildings.Wall;
import core.resources.ResourceContainer;
import objects.buildings.House;
import objects.buildings.Lumberjack;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class Villager extends Worker implements Constructor {

    public final static BufferedImage SPRITE = CustomMethods.loadSprite("src/img/villager.png", GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE).get();
    public final static BufferedImage WORKING_SPRITE = CustomMethods.loadSprite("src/img/villager_working.png", GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE).get();
    public final static BufferedImage SPRITE_MAX = CustomMethods.loadSprite("src/img/villager.png", GameConstants.SPRITE_SIZE_MAX, GameConstants.SPRITE_SIZE_MAX).get();
    public final static BufferedImage WORKING_SPRITE_MAX = CustomMethods.loadSprite("src/img/villager_working.png", GameConstants.SPRITE_SIZE_MAX, GameConstants.SPRITE_SIZE_MAX).get();
    public final static Award BUILT_AWARD = Award.createFreeAward("A baby was born.");

    public final static int VILLAGER_HEALTH = 100;
    public final static int VILLAGER_ENERGY = 5;
    public final static int VILLAGER_SIZE = 1;
    public final static int VILLAGER_SIGHT = 1;
    public final static int VILLAGER_ANIMATION = 1000;

    public final static ResourceContainer VILLAGER_COST = new ResourceContainer(new String[]{"Food", "Water", "Time"}, new int[]{100, 50, 1});
    public final static ResourceContainer VILLAGER_PRODUCTION = new ResourceContainer(new String[]{"Food", "Water", "Wood", "Stone"}, new int[]{5, 5, 5, 5});

    public final static int VILLAGER_DEGRADATION_TIME = 50;
    public final static int VILLAGER_DEGRADATION_AMOUNT = 2;

    public final static int VILLAGER_CYCLE_LENGTH = 12;

    public Villager() {
        super(VILLAGER_ANIMATION, VILLAGER_SIZE, VILLAGER_SIGHT, VILLAGER_HEALTH,
                VILLAGER_DEGRADATION_TIME, VILLAGER_DEGRADATION_AMOUNT, VILLAGER_CYCLE_LENGTH, VILLAGER_ENERGY,
                VILLAGER_COST, VILLAGER_PRODUCTION);
    }

    @Override
    public String getClassLabel() {
        return "Villager";
    }

    @Override
    public String getToken() {
        return "v";
    }

    public OperationsList getOperations(int cycle, OperationCode code) {
        OperationsList operations =  super.getOperations(cycle, code);

        if(code == OperationCode.CONSTRUCTION)
            operations.addAll(getConstructions(cycle));

        return operations;
    }

    @Override
    public OperationsList getConstructions(int cycle) {
        OperationsList constructions = new OperationsList();
        constructions.put("House", _ -> {
            House h = new House();
            addContract(new ConstructContract<>(Villager.this, h));
        });
        constructions.put("Lumberjack", _ -> {
            Lumberjack l = new Lumberjack();
            addContract(new ConstructContract<>(Villager.this, l));
        });
        constructions.put("Wall", _ -> {
            Wall w = new Wall();
            addContract(new ConstructContract<>(Villager.this, w));
        });
        return constructions;
    }

    @Override
    public @NotNull Optional<BufferedImage> getSprite(boolean max) {
        if(getStatus() != Status.WORKING)
            return Optional.of(max ? SPRITE_MAX : SPRITE);
        else
            return Optional.of(max ? WORKING_SPRITE_MAX : WORKING_SPRITE);
    }

    @Override
    public Optional<Award> getConstructionAward() {
        return Optional.of(BUILT_AWARD);
    }

    @Override
    public void addContract(Contract c) throws IllegalArgumentException {
        // Removes current Contract(s), if any
        getContracts().forEach(Contract::abandon);
        getContracts().clear();

        super.addContract(c);
    }
}