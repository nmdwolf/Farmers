package general;

import items.GameObject;

@FunctionalInterface
public interface TypedConsumer {

    void accept(GameObject obj) throws TypeException;

}
