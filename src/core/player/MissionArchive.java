package core.player;

import java.util.ArrayDeque;

// TODO Implement missions
public class MissionArchive extends ArrayDeque<Mission> {

    public MissionArchive() {
        add(new Mission() {
            @Override
            public boolean validate() {
                return false;
            }

            @Override
            public Award getAward() {
                return null;
            }
        });
    }

    public String getFirstDescription() {
        Mission next = peekFirst();
        return (next != null) ? next.toString() : "Story finished!";
    }

}
