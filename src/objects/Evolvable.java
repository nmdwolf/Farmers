package objects;

import core.Award;
import general.OperationsList;

public interface Evolvable {

    public int getLevel();

    public void increaseLevel();

    public Award getEvolveAward();

}
