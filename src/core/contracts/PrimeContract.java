package core.contracts;

import core.GameConstants;
import core.Options;
import core.Type;
import core.TypeException;
import items.GameObject;
import items.units.Worker;

public class PrimeContract<T extends GameObject> extends Contract {

    public final static int PRIME_COST = 1;

    private final T source;

    public PrimeContract(Worker worker, T src) throws TypeException {
        super(worker, GameConstants.PRIME_KEY);

        if(!src.getDescriptions().contains(Type.SOURCE_TYPE))
            throw new TypeException(Type.SOURCE_TYPE);

        source = src;
    }

    @Override
    public boolean work() {
        source.perform(Options.SOURCE_KEY);
        return true;
    }

    @Override
    public int getEnergyCost() {
        return PRIME_COST;
    }
}
