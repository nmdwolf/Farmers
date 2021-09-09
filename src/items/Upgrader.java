package items;

import items.upgrade.Upgrade;

import java.util.List;

public interface Upgrader extends GameObject {

    public List<Upgrade> getUpgrades();

}
