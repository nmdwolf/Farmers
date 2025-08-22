package objects;

import core.player.Award;
import org.jetbrains.annotations.NotNull;

public interface Evolvable {

    public int getLevel();

    public void increaseLevel();

    @NotNull
    public Award getEvolveAward();

}
