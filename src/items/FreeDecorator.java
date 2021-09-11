package items;

import core.Options;
import core.ResourceContainer;
import core.Type;

import static core.Options.CONSTRUCT_KEY;

public class FreeDecorator<T extends GameObject> extends Decorator<T>{

    public FreeDecorator(T constructable) {
        super(constructable);
    }

    @Override
    public T getObject(Type description) {
        return super.getObject(description);
    }

    @Override
    public ResourceContainer getResources(Options option) {
        if(option == Options.CONSTRUCT_KEY)
            return ResourceContainer.EMPTY_CONTAINER;
        else
            return super.getResources(option);
    }

    @Override
    public boolean checkStatus(Options option) {
        if(option == CONSTRUCT_KEY)
            return true;
        else
            return super.checkStatus(option);
    }

    @Override
    public void perform(Options option) {
        if(option == CONSTRUCT_KEY)
            getPlayer().addObject(this);
        else
            super.perform(option);
    }
}
