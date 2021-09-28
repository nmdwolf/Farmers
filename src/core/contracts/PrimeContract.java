package core.contracts;

import core.Option;
import core.Type;
import general.TypeException;
import items.GameObject;
import items.units.Worker;

public class PrimeContract extends Contract {

    public final static int PRIME_COST = 1;

    private final GameObject source;

    public PrimeContract(Worker employee, GameObject src) throws TypeException {
        super(employee);

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
