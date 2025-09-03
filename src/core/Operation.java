package core;

import objects.GameObject;

@FunctionalInterface
public interface Operation {

    void perform(GameObject target);

}
