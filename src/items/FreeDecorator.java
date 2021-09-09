package items;

import core.GameConstants;
import core.Resource;

import java.util.HashMap;
import java.util.Map;

public class FreeDecorator extends Decorator<Constructable> implements Constructable{

    public FreeDecorator(Constructable constructable) {
        super(constructable);
    }

    @Override
    public Constructable getObject(int description) {
        return super.getObject(description);
    }

    @Override
    public Map<Resource, Integer> getCost() {
        return new HashMap<>();
    }

    @Override
    public boolean canConstruct() {
        return true;
    }

    @Override
    public void construct() { getPlayer().addObject(this); }

    @Override
    public boolean isVisible(int cycle) {
        return true;
    }
}
