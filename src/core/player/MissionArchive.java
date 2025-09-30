package core.player;

import objects.units.Villager;

import java.util.ArrayDeque;
import java.util.function.Consumer;
import java.util.function.Function;

// TODO Implement missions
public class MissionArchive extends ArrayDeque<Mission> {

    private final Player player;
    private final ArrayDeque<Mission> completed;

    public MissionArchive(Player p) {
        player = p;
        completed = new ArrayDeque<>();
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
                "Your glorious nation should consist of at least 5 villagers.",
                "You have five villagers in your nation. Congratulations with completing" +
                        " your first mission!",
                p -> p.changeResource("Food", 100));
        add(p -> p.getGained("Food") == 200,
                "You should gather sufficient food to keep your people healthy. 200 seems like a reasonable amount to start with. Cells rich with food are indicated by a beige hue.",
                "You learned how to collect resources.",
                p -> p.changeResource("Food", 200));
    }

    public void validate() {
        Mission current = peekFirst();
        while(current != null) { // TODO Implement alternative
            if (current.validate(player)) {
                player.getAwardArchive().awardExternal(pop().getAward());
                completed.add(current);
                current = peekFirst();
            } else
                break;
        }
    }

    public void add(Function<Player, Boolean> validator, String decription, String award, Consumer<Player> rewarder) {
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

            @Override
            public void reward(Player p) {
                rewarder.accept(p);
            }
        });
    }

    public ArrayDeque<Mission> getCompleted() {
        return completed;
    }
}
