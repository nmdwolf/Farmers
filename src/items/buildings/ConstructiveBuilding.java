package items.buildings;

import core.*;

import java.util.Map;

public abstract class ConstructiveBuilding extends Building {

    private int constructX, constructY;
    private Location constructionLocation;

    public ConstructiveBuilding(Player p, Location loc, ResourceContainer res, Map<Options, Integer> params) {
        super(p, loc, res, params);
        updateDescriptions(Type.CONSTRUCTOR_TYPE);
        constructionLocation = new Location(loc.x + 1, loc.y, loc.z);

        constructX = 1;
    }

    @Override
    public int getValue(Options option) {
        return switch(option) {
            case CONSTRUCT_X_KEY: yield constructX;
            case CONSTRUCT_Y_KEY: yield constructY;
            default: yield super.getValue(option);
        };
    }

    @Override
    public void changeValue(Options option, int amount) {
        switch(option) {
            case CONSTRUCT_X_KEY:
                constructX += amount;
                break;
            case CONSTRUCT_Y_KEY:
                constructY += amount;
                break;
            default:
                super.changeValue(option, amount);
                break;
        }
    }
}
