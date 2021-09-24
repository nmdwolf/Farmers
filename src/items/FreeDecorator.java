package items;

import core.Option;
import general.ResourceContainer;

import static core.Option.CONSTRUCT;

public class FreeDecorator<T extends GameObject> extends Decorator<T>{

    public FreeDecorator(T constructable) {
        super(constructable);
    }

    @Override
    public ResourceContainer getResources(Option option) {
        if(option == Option.CONSTRUCT)
            return ResourceContainer.EMPTY_CONTAINER;
        else
            return super.getResources(option);
    }

    @Override
    public boolean checkStatus(Option option) {
        if(option == CONSTRUCT)
            return true;
        else
            return super.checkStatus(option);
    }

    @Override
    public void perform(Option option) {
        if(option == CONSTRUCT) {
            super.perform(CONSTRUCT);
            getPlayer().changeResources(super.getResources(CONSTRUCT).negate());
        }
        else
            super.perform(option);
    }
}
