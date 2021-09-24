package items.sources;

import core.Location;
import core.Option;
import core.Player;
import core.Type;
import general.OperationsList;
import general.ResourceContainer;
import items.GameObject;
import items.upgrade.EvolveUpgrade;
import items.upgrade.Upgrade;

import java.util.List;
import java.util.Map;

public abstract class Source extends GameObject {

    private boolean primed;

    private final ResourceContainer gain;

    public Source(Player player, Location location, Map<Option, Integer> params, ResourceContainer gains) {
        super(player, location, params);
        gain = gains;

        updateTypes(Type.SOURCE);
    }

    @Override
    public ResourceContainer getResources(Option option) {
        if (option == Option.SOURCE) {
            primed = false;
            return gain;
        } else
            return this.getResources(option);
    }

    @Override
    public void perform(Option option) {
        if (option == Option.SOURCE)
            primed = true;
        else
            super.perform(option);
    }

    @Override
    public boolean checkStatus(Option option) {
        if(option == Option.SOURCE)
            return primed;
        else
            return super.checkStatus(option);
    }

    @Override
    public List<Upgrade> getUpgrades() {
        return null;
    }

    @Override
    public List<GameObject> getProducts() {
        return null;
    }

    @Override
    public List<EvolveUpgrade> getEvolutions() {
        return null;
    }

    @Override
    public OperationsList getOperations() {
        return null;
    }
}
