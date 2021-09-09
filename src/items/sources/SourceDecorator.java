package items.sources;

import core.GameConstants;
import core.Resource;
import items.Decorator;
import items.GameObject;

import java.util.Map;

public class SourceDecorator extends Decorator<GameObject> implements Source{

    private final Map<Resource, Integer> resources;

    public SourceDecorator(GameObject obj, Map<Resource, Integer> res) {
        super(obj);
        resources = res;

        if(getDescriptions().contains(GameConstants.SOURCE_TYPE)) {
            Source source = (Source) obj;
            for(Resource resource : source.getResources().keySet()) {
                if(resources.containsKey(resource))
                    resources.put(resource, source.getResources().get(resource) + resources.get(resource));
                else
                    resources.put(resource, source.getResources().get(resource));
            }
        }
        updateDescriptions(GameConstants.SOURCE_TYPE);
    }

    @Override
    public GameObject getObject(int description) {
        return (description == GameConstants.SOURCE_TYPE) ? this : super.getObject(description);
    }

    @Override
    public Map<Resource, Integer> getResources() {
        return resources;
    }
}
