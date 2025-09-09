package objects.templates;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import core.resources.ResourceContainer;

public class ConstructionTemplate extends Template {

    public ResourceContainer cost;
    public int energyCost;
    public String award;

    @JsonSetter(nulls = Nulls.SKIP)
    public boolean hasVisibleFoundation = true;

}
