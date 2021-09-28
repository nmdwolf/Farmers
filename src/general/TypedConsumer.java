package general;

import items.GameObject;

@FunctionalInterface
public interface TypedConsumer {

    void run(GameObject obj, Object... params) throws TypeException;

}
