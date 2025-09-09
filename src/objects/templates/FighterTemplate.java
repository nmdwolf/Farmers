package objects.templates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FighterTemplate extends Template {

    public int attack, attackCost;

    @JsonSetter(nulls = Nulls.SKIP)
    public int range = 0;

    public FighterTemplate() {
        type = "fighter";
    }
}
