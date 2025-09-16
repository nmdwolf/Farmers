package core;

import java.util.ArrayList;

public class OperationsList extends ArrayList<Operation> {

    public static OperationsList EMPTY_LIST = new OperationsList();

    private final ArrayList<String> descriptions;

    public OperationsList() {
        descriptions = new ArrayList<>();
    }

    public OperationsList(OperationsList list) {
        if(list != null) {
            descriptions = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                descriptions.add(list.getDescription(i));
                add(list.get(i));
            }
        } else
            descriptions = new ArrayList<>();
    }

    public OperationsList(String s, Operation c) {
        this();
        put(s, c);
    }

    public void put(String s, Operation c) {
        descriptions.add(s);
        add(c);
    }

    public void addAll(OperationsList list) {
        for(int i = 0; i < list.size(); i++)
            put(list.getDescription(i), list.get(i));
    }

    public Operation remove(int i) {
        descriptions.remove(i);
        return super.remove(i);
    }

    public String getDescription(int i) {
        return descriptions.get(i);
    }
}
