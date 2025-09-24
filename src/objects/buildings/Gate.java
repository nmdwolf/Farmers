package objects.buildings;

import core.Direction;
import core.OperationCode;
import core.OperationsList;
import core.contracts.Contract;
import objects.Obstruction;
import objects.Operational;
import objects.templates.ConstructionTemplate;
import objects.templates.TemplateFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Gate extends Building<Gate> implements Obstruction, Directional, Operational<Gate> {

    private boolean open;
    private final Direction direction;
    private final Wall src;

    public Gate(Direction direction, Wall src) {
        super((ConstructionTemplate) TemplateFactory.getTemplate("Gate"));
        this.direction = direction;
        this.open = false;
        this.src = src;
    }

    @Override
    public int getObstructionCost() {
        return ((ConstructionTemplate) getTemplate()).obstruction;
    }

    @Override
    public String getClassLabel() {
        return "Gate (" + direction.name() + ")";
    }

    @Override
    public @NotNull Direction getDirection() {
        return direction;
    }

    @Override
    public boolean isActive() {
        return !open;
    }

    @Override
    public String toString() {
        return super.toString() +
                "\n\nOpened: " + open;
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        OperationsList operations = new OperationsList();
        if(code == OperationCode.EXTRA)
            operations.put(open ? "Close" : "Open", _ -> open = !open);
        return operations;
    }

    @Override
    public void work() {

    }

    @Override
    public void addContract(Contract<Gate> c) {

    }

    @Override
    public void transferContract(Contract<Gate> c) {

    }

    @Override
    public ArrayList<Contract<Gate>> getContracts() {
        return null;
    }

    @Override
    public void seizeActions() {

    }

    @Override
    public void handleCompletion() {
        getPlayer().removeObject(src);
    }
}
