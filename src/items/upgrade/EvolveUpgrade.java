package items.upgrade;

import core.Option;
import core.Type;
import general.ResourceContainer;
import general.CustomMethods;
import general.TypeException;
import general.TypedConsumer;
import items.GameObject;

public class EvolveUpgrade<T extends GameObject> extends InstanceUpgrade<T> {

    public final static int EVOLVE_ID = CustomMethods.getNewUpgradeIdentifier();

    private final TypedConsumer evolution;

    public EvolveUpgrade(T obj, ResourceContainer res, int cycleThreshold, TypedConsumer task) {
        super(obj, res, cycleThreshold);
        evolution = task;
    }

    @Override
    public void applyTo(GameObject object) {
        object.changeValue(Option.LEVEL, 1);
        object.getPlayer().enable(object.getAward(Option.ENABLED));
        try {
            object.typedDo(Type.EVOLVABLE, evolution);
        } catch (TypeException e) {
            System.out.println("Error occurred while trying to evolve Object of class " + object.getClassIdentifier());
            e.printStackTrace();
        }
    }

    @Override
    public int getID() {
        return EVOLVE_ID;
    }

    @Override
    public String toString() {
        return "Evolve";
    }
}
