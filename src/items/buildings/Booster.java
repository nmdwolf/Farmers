package items.buildings;

import core.*;
import general.ResourceContainer;

import java.util.HashMap;

public abstract class Booster extends Building{

    private final HashMap<Option, Integer> boosts;

    public Booster(Player p, Location loc, ResourceContainer cost, HashMap<Option, Integer> params) {
        super(p, loc, cost, params);
        updateTypes(Type.BOOSTER);

        boosts = new HashMap<>();
        for(Resource res : Resource.values()) {
            Integer value = params.get(res.operation);
            boosts.put(res.operation, value == null ? 0 : value);
        }
    }

    @Override
    public int getValue(Option option) {
        if(boosts.containsKey(option))
            return boosts.get(option);
        else
            return super.getValue(option);
    }
}
