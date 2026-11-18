package objects.templates;

public class UnitTemplate extends Template {

    public final static int UNIT_DIFFICULTY = 1;

    public UnitTemplate() {
        super();
        hasVisibleFoundation = false;
        energyCost = UNIT_DIFFICULTY;
    }

}
