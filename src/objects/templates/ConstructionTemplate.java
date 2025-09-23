package objects.templates;

import core.resources.ResourceContainer;

public class ConstructionTemplate extends Template {

    public ResourceContainer cost;
    public int energyCost;
    public String award;

    public int obstruction = 0;
    public boolean hasVisibleFoundation = true;

}
