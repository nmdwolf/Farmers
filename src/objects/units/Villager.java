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
import objects.templates.ConstructionTemplate;
import objects.templates.TemplateFactory;
import objects.templates.UnitTemplate;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class Villager extends Worker implements Constructor {

    public final static BufferedImage SPRITE = CustomMethods.loadSprite("src/img/villager.png", GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE).get();
    public final static BufferedImage WORKING_SPRITE = CustomMethods.loadSprite("src/img/villager_working.png", GameConstants.SPRITE_SIZE, GameConstants.SPRITE_SIZE).get();
    public final static BufferedImage SPRITE_MAX = CustomMethods.loadSprite("src/img/villager.png", GameConstants.SPRITE_SIZE_MAX, GameConstants.SPRITE_SIZE_MAX).get();
    public final static BufferedImage WORKING_SPRITE_MAX = CustomMethods.loadSprite("src/img/villager_working.png", GameConstants.SPRITE_SIZE_MAX, GameConstants.SPRITE_SIZE_MAX).get();
    public final static Award BUILT_AWARD = Award.createFreeAward("A baby was born.");

    public Villager() {
        super((UnitTemplate) TemplateFactory.getTemplate("Villager"));
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