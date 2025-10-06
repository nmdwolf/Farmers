package core.player;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class AwardArchive {

    private final HashSet<Award> obtainable, obtained, newAwards;
    private final Player player;

    public AwardArchive(Player p) {
        obtainable = new HashSet<>();
        obtained = new HashSet<>();
        newAwards = new HashSet<>();
        player = p;
        initialize();
    }

    public HashSet<Award> validate() {
        for(Award a : obtainable)
            if(a.supplier().getAsBoolean())
                newAwards.add(a);

        HashSet<Award> awarded =
                obtainable.stream().filter(newAwards::contains).collect(Collectors.toCollection(HashSet::
        new));

        obtainable.removeAll(awarded);
        return awarded;
    }

    private void initialize() {
        obtainable.add(Award.createAward("You have mined stones for the very first time.", () -> player.getGained("Stone") > 0));
    }

    public List<String> getNewAwards() {
        List<String> descriptions = newAwards.stream().map(Award::description).collect(Collectors.toList());
        obtained.addAll(newAwards);
        newAwards.clear();
        return descriptions;
    }

    public List<String> getAwards() {
        return obtained.stream().map(Award::description).collect(Collectors.toList());
    }

    public void awardExternal(Award a) {
        if(!obtained.contains(a))
            newAwards.add(a);
    }
}
