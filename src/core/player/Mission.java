package core.player;

public interface Mission {

    boolean validate(Player p);

    Award getAward();

    String getDescription();

    void reward(Player p);

    Mission[] getChildren();
}
