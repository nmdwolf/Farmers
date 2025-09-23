package objects.units;

import core.*;
import core.contracts.ConstructContract;
import UI.CustomMethods;
import core.OperationsList;
import core.contracts.Contract;
import objects.Constructor;
import core.Status;
import objects.buildings.BasicBuilding;
import objects.buildings.Wall;
import objects.templates.TemplateFactory;
import objects.templates.UnitTemplate;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.stream.Collectors;

public class Villager extends Worker implements Constructor {

    public final static BufferedImage WORKING_SPRITE_MAX = CustomMethods.loadSprite("src/img/villager_working.png", GameConstants.SPRITE_SIZE_MAX, GameConstants.SPRITE_SIZE_MAX).orElseThrow();

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
        constructions.put("House", _ ->
            addContract(new ConstructContract<>(Villager.this, BasicBuilding.createBuilding("House"))));
        constructions.put("Lumberjack", _ -> addContract(new ConstructContract<>(Villager.this, BasicBuilding.createBuilding("Lumberjack"))));

        var directions = getCell().getObjects().stream()
                .filter(Wall.class::isInstance)
                .map(obj -> ((Wall)obj).getDirection())
                .collect(Collectors.toSet());
        for(Direction dir : Direction.values()) {
            if(!directions.contains(dir))
                constructions.put("Wall (" + dir.name().charAt(0) + ")", _ -> {
                    Wall w = new Wall(dir);
                    addContract(new ConstructContract<>(Villager.this, w));
                });
        }
        return constructions;
    }

    @Override
    public @NotNull Optional<BufferedImage> getSprite() {
        if(getStatus() != Status.WORKING)
            return super.getSprite();
        else
            return Optional.of(WORKING_SPRITE_MAX);
    }

    // TODO Update cell panel on abandon (Test: Start construction and, then, start another one with same worker)
    @Override
    public void addContract(Contract<Worker> c) throws IllegalArgumentException {
        // Removes current Contract(s), if any
        getContracts().forEach(Contract::abandon);
        getContracts().clear();

        super.addContract(c);
    }
}