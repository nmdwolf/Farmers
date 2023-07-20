package objects;

import core.Award;
import general.OperationsList;

public interface Evolvable {

    public OperationsList getEvolutions(int cycle);

    public int getLevel();

    public void increaseLevel();

    public Award getEvolveAward();

}
