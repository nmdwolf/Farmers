package items;

import core.Player;

public interface GameObject {

    public int getX();
    public int getY();
    public void setX(int x);
    public void setY(int y);
    public int getViewLevel();
    public void setViewLevel(int level);

    public Player getPlayer();

    public String getType();
    public String getToken();

    public void reset();
}
