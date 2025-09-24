package objects;

import core.player.Award;
import core.resources.ResourceContainer;
import objects.templates.ConstructionTemplate;

import java.util.Optional;

public abstract class Construction<T extends Construction<T>> extends GameObject<T> {

    private final int energyCost, constructionTime;
    private final boolean hasVisibleFoundation;
    private final ResourceContainer cost;

    public Construction(ConstructionTemplate template) {
        super(template);

        this.cost = template.cost;
        constructionTime = cost.get("Time");

        this.energyCost = template.energyCost;
        this.hasVisibleFoundation = template.hasVisibleFoundation;
    }

    public int getConstructionTime() { return constructionTime; }

    public ResourceContainer getCost() { return cost; }

    public int getEnergyCost() {
        return energyCost;
    }

    public boolean hasVisibleFoundation() { return hasVisibleFoundation; }

    public Optional<Award> getConstructionAward() { return Optional.of(Award.createFreeAward(((ConstructionTemplate)getTemplate()).award)); }

    /**
     *
     */
    public void handleCompletion() {};
}
