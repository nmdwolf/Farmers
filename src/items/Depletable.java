package items;

import core.Contract;

public interface Depletable extends GameObject {

    public int getEnergy();

    public int getMaxEnergy();

    public void changeEnergy(int amount);

    public void changeMaxEnergy(int amount);
}
