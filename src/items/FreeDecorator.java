package items;

import core.Option;
import core.Type;
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
            getPlayer().changeResources(super.getResources(CONSTRUCT).negate());
            if(getTypes().contains(Type.BUILDING)) {
                for(int i = 0; i < super.getValue(CONSTRUCT); i++)
                    super.perform(CONSTRUCT);
            } else
                super.perform(CONSTRUCT);
        }
        else
            super.perform(option);
    }

    @Override
    public int getValue(Option option) {
        if(option == CONSTRUCT)
            return 0;
        else
            return super.getValue(option);
    }
}
