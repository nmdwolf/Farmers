package core.player;

import objects.units.Villager;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

// TODO Implement missions
public class MissionArchive {

    private final Player player;
    private final ArrayDeque<Mission> remaining, completed;

    public MissionArchive(Player p) {
        player = p;
        remaining = new ArrayDeque<>();
        completed = new ArrayDeque<>();
        initialize();
    }

    public void add(Mission m) {
        remaining.add(m);
    }

    public String getNextDescription() {
        Mission next = remaining.peekFirst();
        return (next != null) ? next.getDescription() : "Story finished!";
    }

    /**
     * This method sets up a standard {@code MissionArchive} with default missions to guide the first playthrough.
     */
    public void initialize() {
        Mission m3 = constructMission(p -> p.getObjects().stream().filter(obj -> obj.getTemplate().type.equals("House")).count() == 2,
                "Improve the housing conditions in your nation. Build at least 2 houses.",
                "Your villagers have a roof over there head now. Well done!",
                p -> p.changeResource("Wood", 100),
                null);
        Mission m2 = constructMission(p -> p.getGained("Food") == 200,
                "You should gather sufficient food to keep your people healthy. 200 seems like a reasonable amount to start with. Cells rich with food are indicated by a beige hue.",
                "You learned how to collect resources.",
                p -> p.changeResource("Food", 200),
                new Mission[]{m3});
        Mission m1 = constructMission(p -> p.getObjects().stream().filter(Villager.class::isInstance).count() == 5,
                "Your glorious nation should consist of at least 5 villagers.",
                "You have five villagers in your nation. Congratulations with completing" +
                        " your first mission!",
                p -> p.changeResource("Food", 100),
                new Mission[]{m2});

        remaining.add(m1);
    }

    public void validateMissions() {
        Mission current = remaining.peekFirst();
        while(current != null) { // TODO Implement alternative
            if (current.validate(player)) {
                player.getAwardArchive().awardExternal(remaining.pop().getAward());
                current.reward(player);
                completed.add(current);
                remaining.addAll(Arrays.stream(current.getChildren()).toList());
                current = remaining.peekFirst();
            } else
                break;
        }
    }

    public ArrayDeque<Mission> getRemaining() {
        ArrayDeque<Mission> deque = new ArrayDeque<>();
        ArrayDeque<Mission> result = new ArrayDeque<>();
        if(!remaining.isEmpty()) {
            deque.add(remaining.peekFirst());
            while (!deque.isEmpty()) {
                Mission current = deque.pop();
                deque.addAll(Arrays.asList(current.getChildren()));
                result.add(current);
            }
        }
        return result;
    }

    public ArrayDeque<Mission> getCompleted() {
        return completed;
    }

    public static Mission constructMission(Function<Player, Boolean> validator, String decription, String award, Consumer<Player> rewarder, Mission[] children) {
        return new Mission() {
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

            @Override
            public Mission[] getChildren() {
                if(children != null)
                    return children;
                else
                    return new Mission[0];
            }
        };
    }
}
