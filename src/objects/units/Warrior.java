package objects.units;

import core.OperationCode;
import core.OperationsList;
import objects.templates.TemplateFactory;
import objects.templates.UnitTemplate;

public class Warrior extends Unit {

    public Warrior(UnitTemplate template) {
        super(template);
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
