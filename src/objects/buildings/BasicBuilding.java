package objects.buildings;

import objects.templates.ConstructionTemplate;
import objects.templates.TemplateFactory;

public class BasicBuilding extends Building<BasicBuilding> {

    public BasicBuilding(ConstructionTemplate temp) {
        super(temp);
    }

    public static BasicBuilding createBuilding(String className) throws IllegalArgumentException {
        if(TemplateFactory.isRegistered(className))
            return new BasicBuilding((ConstructionTemplate) TemplateFactory.getTemplate(className));
        throw new IllegalArgumentException("The provided class " + className + " is unknown.");
    }

}
