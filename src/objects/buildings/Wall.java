package objects.buildings;

import core.Direction;
import objects.Obstruction;
import objects.templates.ConstructionTemplate;
import objects.templates.TemplateFactory;
import org.jetbrains.annotations.NotNull;

public class Wall extends IdleBuilding<Wall> implements Obstruction, Directional { ;

    private final Direction direction;

    public Wall(Direction direction) {
        super((ConstructionTemplate) TemplateFactory.getTemplate("Wall"));
        this.direction = direction;
    }

    @Override
    public String getToken() {
        return "||";
    }

    @Override
    public @NotNull Direction getDirection() {
        return direction;
    }
}
