package items;

import core.Contract;

public interface Worker extends GameObject{

    public void work();

    public void addContract(Contract c);

    public boolean hasContract();
}
