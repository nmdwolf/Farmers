package items;

import core.Contract;

public interface Productive extends GameObject{

    public void work();

    public void addContract(Contract c);

}
