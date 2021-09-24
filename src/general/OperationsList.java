package general;

import java.util.ArrayList;

public class OperationsList extends ArrayList<TypedConsumer> {

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

    public void put(String s, TypedConsumer c) {
        descriptions.add(s);
        add(c);
    }

    public TypedConsumer remove(int i) {
        descriptions.remove(i);
        return super.remove(i);
    }

    public String getDescription(int i) {
        return descriptions.get(i);
    }
}
