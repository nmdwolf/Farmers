package core.resources;

import objects.Construction;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.Optional;

public class SourceDecorator extends Construction implements Source {

    private final Construction obj;
    private ResourceContainer yield;
    private final String type;

    public SourceDecorator(Construction obj, String type, int amount) {
        super(obj.getSize(), obj.getSight(),
                obj.getMaxHealth(), obj.getDegradeTime(), obj.getDegradeAmount(),
                obj.getCost(), obj.getAttackCost(), obj.hasVisibleFoundation());

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
    public String getResourceType() {
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
