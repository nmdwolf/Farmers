package items;

import java.util.HashMap;

public class Base extends Building {

    public final static int BASE_HEALTH = 1000;

    public Base(Player p, int x, int y) {
        super(p, new HashMap<>() {{
            put(X_KEY, x);
            put(Y_KEY, y);
            put(HEALTH_KEY, BASE_HEALTH);
            put(STATUS_KEY, FOUNDATION);
        }});
    }

    @Override
    public String toString() {
        return "Base";
    }

    @Override
    public String getToken() {
        return "Base";
    }
}
