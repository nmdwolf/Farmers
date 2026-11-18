package objects.buildings;

import core.Direction;
import objects.Obstruction;
import objects.templates.TemplateFactory;
import org.jetbrains.annotations.NotNull;

public class Wall extends Building implements Obstruction, Directional { ;

    private final Direction direction;

    public Wall(Direction direction) {
        super(TemplateFactory.getTemplate("Wall"));
        this.direction = direction;
    }

    @Override
    public int getObstructionCost() {
        return getTemplate().obstruction;
    }

    @Override
    public String getToken() {
        return "||";
    }

    @Override
    public String getClassLabel() {
        return "Wall (" + direction.name() + ")";
    }

    @Override
    public @NotNull Direction getDirection() {
        return direction;
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
