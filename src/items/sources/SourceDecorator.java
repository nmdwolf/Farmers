package items.sources;

import core.GameConstants;
import core.Options;
import core.ResourceContainer;
import core.Type;
import items.Decorator;
import items.GameObject;

public class SourceDecorator extends Decorator<GameObject> {

    private final ResourceContainer gain;
    private boolean primed;

    public SourceDecorator(GameObject obj, ResourceContainer res) {
        super(obj);
        gain = res.add(obj.getResources(Options.SOURCE_KEY));
        updateDescriptions(Type.SOURCE_TYPE);
    }

    @Override
    public GameObject getObject(Type description) {
        return (description == Type.SOURCE_TYPE) ? this : super.getObject(description);
    }

    @Override
    public ResourceContainer getResources(Options option) {
        if(option == Options.SOURCE_KEY) {
            ResourceContainer gains = primed ? gain : ResourceContainer.EMPTY_CONTAINER;
            primed = false;
            return gains;
        } else
            return super.getResources(option);
    }
}
