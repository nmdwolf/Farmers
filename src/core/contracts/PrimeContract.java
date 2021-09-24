package core.contracts;

import core.GameConstants;
import core.Option;
import core.Type;
import general.TypeException;
import items.GameObject;
import items.units.Worker;

public class PrimeContract<T extends GameObject> extends Contract {

    public final static int PRIME_COST = 1;

    private final T source;

    public PrimeContract(Worker worker, T src) throws TypeException {
        super(worker, GameConstants.PRIME_KEY);

        if(!src.getTypes().contains(Type.SOURCE))
            throw new TypeException(Type.SOURCE);

        source = src;
    }

    @Override
    public boolean work() {
        source.perform(Option.SOURCE);
        return true;
    }

    @Override
    public int getEnergyCost() {
        return PRIME_COST;
    }
}
