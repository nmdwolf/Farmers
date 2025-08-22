package core.player;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AwardSystem extends HashSet<Award>{

    private HashSet<Award> newAwards;

    public AwardSystem() {
        newAwards = new HashSet<>();
    }

    public boolean hasEnabled(Award award) { return contains(award); }

    public void enable(Award award) {
        if(award != null && !contains(award))
            newAwards.add(award);
    }

    public Set<String> getNewAwards() {
        Set<String> descriptions = newAwards.stream().map(Award::description).collect(Collectors.toSet());
        addAll(newAwards);
        newAwards = new HashSet<>();
        return descriptions;
    }
}
