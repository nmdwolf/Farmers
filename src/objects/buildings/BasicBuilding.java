package objects.buildings;

import objects.templates.Template;
import objects.templates.TemplateFactory;

public class BasicBuilding extends Building {

    public BasicBuilding(Template temp) {
        super(temp);
    }

    public static BasicBuilding createBuilding(String className) throws IllegalArgumentException {
        if(TemplateFactory.isRegistered(className))
            return new BasicBuilding(TemplateFactory.getTemplate(className));
        throw new IllegalArgumentException("The provided class " + className + " is unknown.");
    }

}
