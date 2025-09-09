package objects.templates;

public class UnitTemplate extends ConstructionTemplate {

    public final static int UNIT_DIFFICULTY = 1;

    public int animationDelay;
    public int cycleLength, energy;

    public UnitTemplate() {
        hasVisibleFoundation = false;
        energyCost = UNIT_DIFFICULTY;
    }

}
