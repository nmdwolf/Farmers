package items.sources;

import core.Option;
import general.ResourceContainer;
import items.Decorator;
import items.GameObject;

public class SourceDecorator extends Decorator<GameObject> {

    private final ResourceContainer gain;
    private boolean primed;

    public SourceDecorator(GameObject obj, ResourceContainer res) {
        super(obj);
        gain = res.add(obj.getResources(Option.SOURCE));
    }

    @Override
    public ResourceContainer getResources(Option option) {
        if(option == Option.SOURCE) {
            ResourceContainer gains = (primed ? gain : ResourceContainer.EMPTY_CONTAINER);
            primed = false;
            return gains;
        } else
            return super.getResources(option);
    }

    @Override
    public void perform(Option option) {
        super.perform(option);
        if(option == Option.SOURCE)
            primed = true;
    }

    @Override
    public boolean checkStatus(Option option) {
        if(option == Option.SOURCE)
            return primed;
        else
            return super.checkStatus(option);
    }
}
