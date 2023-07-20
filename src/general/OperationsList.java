package general;

import core.Operation;
import core.upgrade.Upgrade;

import java.util.ArrayList;

public class OperationsList extends ArrayList<Operation> {

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

    public void put(String s, Operation c) {
        descriptions.add(s);
        add(c);
    }

    /**
     * Adds an Upgrade as an Operation if the Upgrade is visible
     * @param s textual information about the core.upgrade
     * @param u core.upgrade
     */
    public void putUpgrade(String s, Upgrade u) {
        if(u != null && u.isVisible())
            put(s, () -> {
                if(u.isPossible())
                    u.upgrade();
            });
    }

    public Operation remove(int i) {
        descriptions.remove(i);
        return super.remove(i);
    }

    public String getDescription(int i) {
        return descriptions.get(i);
    }
}
