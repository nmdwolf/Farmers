package objects.units;

import core.*;
import core.contracts.ConstructContract;
import core.OperationsList;
import objects.Constructor;
import objects.buildings.BasicBuilding;
import objects.buildings.Gate;
import objects.buildings.Wall;
import objects.templates.TemplateFactory;
import objects.templates.UnitTemplate;
import java.util.stream.Collectors;

public class Villager extends Worker implements Constructor {

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

        var walls = getCell().getObjects().stream()
                .filter(Wall.class::isInstance)
                .map(Wall.class::cast)
                .collect(Collectors.toMap(Wall::getDirection, w -> w));
        for(Direction dir : Direction.values()) {
            if(!walls.containsKey(dir))
                constructions.put("Wall (" + dir.name().charAt(0) + ")", _ -> {
                    addContract(new ConstructContract<>(Villager.this, new Wall(dir)));
                });
            else
                constructions.put("Gate (" + dir.name().charAt(0) + ")", _ -> {
                    addContract(new ConstructContract<>(Villager.this, new Gate(dir, walls.get(dir))));
                });
        }

        return constructions;
    }
}