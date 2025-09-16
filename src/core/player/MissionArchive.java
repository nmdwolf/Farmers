package core.player;

import objects.units.Villager;

import java.util.ArrayDeque;

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
        add(new Mission() {
            @Override
            public boolean validate(Player p) {
                return p.getObjects().stream().filter(obj -> obj instanceof Villager).count() == 5;
            }

            @Override
            public Award getAward() {
                return Award.createFreeAward("You have five villagers in your nation. Congratulations with completing" +
                        " your first mission!");
            }

            @Override
            public String getDescription() {
                return "Your glorious nation must consist of at least 5 villagers.";
            }
        });
    }

    public void validate() {
        Mission current = peekFirst();
        if(current != null) // TODO Implement alternative
            if(current.validate(player))
                player.getAwardArchive().awardExternal(pop().getAward());
    }

}
