package core.player;

import objects.units.Villager;

import java.util.ArrayDeque;
import java.util.function.Function;

// TODO Implement missions
public class MissionArchive extends ArrayDeque<Mission> {

    private final Player player;

    public MissionArchive(Player p) {
        player = p;
        initialize();
    }

    public String getNextDescription() {
        Mission next = peekFirst();
        return (next != null) ? next.getDescription() : "Story finished!";
    }

    /**
     * This method sets up a standard {@code MissionArchive} with default missions to guide the first playthrough.
     */
    public void initialize() {
        add(p -> p.getObjects().stream().filter(obj -> obj instanceof Villager).count() == 5,
                "Your glorious nation must consist of at least 5 villagers.",
                "You have five villagers in your nation. Congratulations with completing" +
                        " your first mission!");
        add(p -> p.getGained("Food") == 200, "You should gather sufficient food to keep your people healthy. 200 seems like a reasonable amount to start with. Cells rich with food are indicated by a beige hue.", "You learned how to collect resources.");
    }

    public void validate() {
        Mission current = peekFirst();
        if(current != null) // TODO Implement alternative
            if(current.validate(player))
                player.getAwardArchive().awardExternal(pop().getAward());
    }

    public void add(Function<Player, Boolean> validator, String decription, String award) {
        add(new Mission() {
            @Override
            public boolean validate(Player p) {
                return validator.apply(p);
            }

            @Override
            public Award getAward() {
                return Award.createFreeAward(award);
            }

            @Override
            public String getDescription() {
                return decription;
            }
        });
    }
}
