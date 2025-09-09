package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import core.resources.ResourceContainer;

public class Source extends Loadout implements core.resources.Source {

    private final ResourceContainer source;

    @JsonCreator
    public Source(ResourceContainer source) {
        super("source");
        this.source = source;
    }

    @Override
    public ResourceContainer getSources() {
        return source;
    }

    @Override
    public String toString() {
        return "Source: {" + source + "}";
    }
}
