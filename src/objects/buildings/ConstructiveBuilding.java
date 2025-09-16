package objects.buildings;

import core.OperationsList;
import core.*;
import core.contracts.Contract;
import objects.Constructor;
import objects.Operational;
import objects.templates.ConstructionTemplate;

import java.util.ArrayList;

public abstract class ConstructiveBuilding<B extends ConstructiveBuilding<B>> extends Building<B> implements Constructor, Operational<B> {

    private int x, y;
    public ConstructiveBuilding(ConstructionTemplate temp, int x, int y) {
        super(temp);
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public OperationsList getOperations(int cycle, OperationCode code) {
        if(code == OperationCode.CONSTRUCTION)
            return getConstructions(cycle);
        else
            return new OperationsList();
    }

    // TODO implement contracts
    @Override
    public void work() {}

    // TODO implement contracts
    @Override
    public void seizeActions() {

    }

    // TODO implement contracts
    @Override
    public ArrayList<Contract<B>> getContracts() {
        return null;
    }

    @Override
    public void transferContract(Contract<B> c) {}

    // TODO implement contracts
    @Override
    public void addContract(Contract<B> c) {

    }
}
