package core;

public class TypeException extends Exception {

    public TypeException(Type type) {
        super("Expected object of type " + type + ", but none was found.");
    }

}
