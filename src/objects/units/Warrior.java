package objects.units;

import core.OperationCode;
import core.OperationsList;
import objects.Aggressive;
import objects.GameObject;
import objects.loadouts.Fighter;
import objects.templates.TemplateFactory;
import objects.templates.UnitTemplate;

public class Warrior extends Unit implements Aggressive {

    public Warrior(UnitTemplate template) {
        super(template);
    }

    @Override
    public int getRange() {
        return getLoadout(Fighter.class).map(Fighter::getRange).orElse(0);
    }

    @Override
    public int getAttack() {
        return getLoadout(Fighter.class).map(Fighter::getAttack).orElse(0);
    }

    @Override
    public int getAttackCost() {
        return getLoadout(Fighter.class).map(Fighter::getAttackCost).orElse(0);
    }

    @Override
    public void attack(GameObject obj) {
        getLoadout(Fighter.class).ifPresent(l -> l.attack(obj));
    }

    @Override
    public void changeAttack(int amount) {
        getLoadout(Fighter.class).ifPresent(l -> l.changeAttack(amount));
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        return OperationsList.EMPTY_LIST;
    }

    public static Warrior createWarrior(String className) throws IllegalArgumentException {
        if(TemplateFactory.isRegistered(className))
            return new Warrior((UnitTemplate)TemplateFactory.getTemplate(className));
        throw new IllegalArgumentException("The provided class " + className + " is unknown.");
    }
}
