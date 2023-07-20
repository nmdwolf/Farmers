package objects.resources;

import core.Award;
import general.OperationsList;
import objects.Constructable;

import java.awt.image.BufferedImage;

public class SourceDecorator extends Constructable implements Source{

    private final Constructable obj;
    private ResourceContainer yield;
    private final Resource type;

    public SourceDecorator(Constructable obj, Resource type, int amount) {
        super(obj.getPlayer(), obj.getCell(), obj.getStartCycle(), obj.getSpace(), obj.getSight(),
                obj.getMaxHealth(), obj.getDegradeTime(), obj.getDegradeAmount(),
                obj.getCost(), obj.getDifficulty(), obj.hasVisibleFoundation());

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
    public Award getConstructionAward() {
        return null;
    }

    @Override
    public OperationsList getEvolutions(int cycle) {
        return obj.getEvolutions(cycle);
    }

    @Override
    public Award getEvolveAward() {
        return obj.getEvolveAward();
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
    public BufferedImage getSprite() {
        return obj.getSprite();
    }

    @Override
    public OperationsList getOperations(int cycle) {
        return obj.getOperations(cycle);
    }
}
