package objects.templates;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import core.resources.ResourceContainer;

public class ConstructionTemplate extends ObjectTemplate {

    public ResourceContainer cost;
    public int energyCost;

    @JsonSetter(nulls = Nulls.SKIP)
    public boolean hasVisibleFoundation = true;

}
