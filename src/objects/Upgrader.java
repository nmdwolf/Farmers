package objects;

import general.OperationsList;

public interface Upgrader {

    public OperationsList getUpgrades(int cycle);

}
