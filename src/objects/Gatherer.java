package objects;

public interface Gatherer {

    /**
     * Calculates the production yield of the provided resource, taking into account current {@code Booster}s.
     * @param resource Resource for which the production yield is calculated.
     * @return production yield
     */
    int getYield(String resource);

    int getGatherCost();
}
