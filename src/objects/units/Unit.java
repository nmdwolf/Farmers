package objects.units;

import objects.Operational;
import objects.templates.UnitTemplate;

import static core.InternalSettings.COLD_LEVEL;
import static core.InternalSettings.UNIT_TYPE;

public abstract class Unit extends Operational {

    public Unit(UnitTemplate temp) {
        super(temp);
    }

    @Override
    public void cycle(int cycle) {
        super.cycle(cycle);
        increaseHealth(Math.min(0, getCell().getHeatLevel() - COLD_LEVEL));
    }

    @Override
    public int getType() {
        return UNIT_TYPE;
    }

}
