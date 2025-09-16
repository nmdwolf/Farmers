package objects.buildings;

import objects.Spacer;
import core.upgrade.*;
import objects.templates.ConstructionTemplate;
import objects.templates.TemplateFactory;
import java.util.ArrayList;

public class House extends IdleBuilding<House> implements Upgradable, Spacer {

    private int space;

    public House() {
        super((ConstructionTemplate) TemplateFactory.getTemplate("House"));
        if(getLoadout(objects.loadouts.Spacer.class).isEmpty())
            throw new IllegalArgumentException("Template does not contain a space attribute.");
    }

    @Override
    public ArrayList<Upgrade> getUpgrades() {
        ArrayList<Upgrade> upgrades = new ArrayList<>();
        upgrades.add(new LeatherUpgrade(getPlayer()));
        return upgrades;
    }

    @Override
    public int getSpaceBoost() {
        return getLoadout(objects.loadouts.Spacer.class).get().getSpaceBoost();
    }

    @Override
    public void changeSpaceBoost(int amount) {
        getLoadout(objects.loadouts.Spacer.class).get().changeSpaceBoost(amount);
    }
}
