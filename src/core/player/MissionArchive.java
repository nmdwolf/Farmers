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

    public String getFirstDescription() {
        Mission next = peekFirst();
        return (next != null) ? next.toString() : "Story finished!";
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
                return Award.createAward("You have five villagers in your nation. Congratulations with completing your first mission!");
            }
        });
    }

    public void validate() {
        Mission current = peekFirst();
        if(current.validate(player))
            player.enableAward(pop().getAward());
    }

}
