package items;

import core.Contract;

public interface Depletable extends GameObject {

    public int getEnergy();

    public void changeEnergy(int amount);
}
