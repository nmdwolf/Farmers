package objects.units;

import core.*;
import core.contracts.ConstructContract;
import UI.CustomMethods;
import core.OperationsList;
import core.contracts.Contract;
import objects.Constructor;
import core.Status;
import objects.buildings.Building;
import objects.buildings.Wall;
import objects.buildings.House;
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

    public Villager() {
        super((UnitTemplate) TemplateFactory.getTemplate("Villager"));
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
            addContract(new ConstructContract<>(Villager.this, Building.createBuilding("Lumberjack")));
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
            return super.getSprite(max);
        else
            return Optional.of(max ? WORKING_SPRITE_MAX : WORKING_SPRITE);
    }


    @Override
    public void addContract(Contract c) throws IllegalArgumentException {
        // Removes current Contract(s), if any
        getContracts().forEach(Contract::abandon);
        getContracts().clear();

        super.addContract(c);
    }
}