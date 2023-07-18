package items;

import core.Award;
import general.OperationsList;

public interface Evolvable {

    public OperationsList getEvolutions();

    public int getLevel();

    public void increaseLevel();

    public Award getEvolveAward();

}
