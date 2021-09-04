package items;

import core.GameConstants;
import core.Player;

import java.util.HashMap;

public class Base extends Building {

    public final static int BASE_HEALTH = 1000;
    public final static int BASE_SPACE = 5;
    public final static int BASE_SIZE = 5;

    public Base(Player p, int x, int y) {
        super(p, new HashMap<>() {{
            put(GameConstants.X_KEY, x);
            put(GameConstants.Y_KEY, y);
            put(GameConstants.HEALTH_KEY, BASE_HEALTH);
            put(GameConstants.STATUS_KEY, GameConstants.FOUNDATION);
            put(GameConstants.SPACE_KEY, BASE_SPACE);
            put(GameConstants.SIZE_KEY, BASE_SIZE);
            put(GameConstants.VIEW_KEY, p.getViewLevel());
        }});
    }

    @Override
    public String toString() {
        return "Base";
    }

    @Override
    public String getType() {
        return "Base";
    }

    @Override
    public String getToken() {
        return "Base";
    }

    @Override
    public void reset() {

    }
}
