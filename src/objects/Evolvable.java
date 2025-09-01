package objects;

import core.player.Award;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Evolvable {

    int getLevel();

    void increaseLevel();

    default @NotNull Optional<Award> getEvolveAward() { return Optional.empty(); }

}
