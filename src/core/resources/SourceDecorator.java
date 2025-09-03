package core.resources;

import core.player.Award;
import core.OperationCode;
import core.OperationsList;
import objects.Construction;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class SourceDecorator extends Construction implements Source {

    private final Construction obj;
    private ResourceContainer yield;
    private final Resource type;

    public SourceDecorator(Construction obj, Resource type, int amount) {
        super(obj.getPlayer(), obj.getCell(), obj.getStartCycle(), obj.getSpace(), obj.getSight(),
                obj.getMaxHealth(), obj.getDegradeTime(), obj.getDegradeAmount(),
                obj.getCost(), obj.getEnergyCost(), obj.hasVisibleFoundation());

        if(obj instanceof Source)
            throw new IllegalArgumentException("GameObject is already of type Source.");
        this.obj = obj;
        this.type = type;
        this.yield = new ResourceContainer(type, amount);
    }

    @Override
    public ResourceContainer getYield() {
        return yield;
    }

    @Override
    public void cycle(int cycle) {
        obj.cycle(cycle);
    }

    @Override
    public Resource getResourceType() {
        return type;
    }

    @Override
    public String getClassLabel() {
        return obj.getClassLabel();
    }

    @Override
    public String getToken() {
        return obj.getToken();
    }

    @Override
    public @NotNull Optional<BufferedImage> getSprite(boolean max) {
        return obj.getSprite(max);
    }

    @Override
    public int getType() {
        return obj.getType();
    }
}
