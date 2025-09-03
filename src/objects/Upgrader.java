package objects;

import core.OperationsList;

public interface Upgrader {

    public OperationsList getUpgrades(int cycle);

}
