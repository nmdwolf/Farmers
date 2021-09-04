package items;

import core.Contract;
import core.Player;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class ProductiveUnit extends Unit implements Productive{

    private ArrayList<Contract> contracts;

    public ProductiveUnit(Player p, HashMap<Integer, Integer> params) {
        super(p, params);
        contracts = new ArrayList<>();
    }

    @Override
    public void work() {
        for(Contract c : contracts) {
            if(getEnergy() >= c.getCost()) {
                changeEnergy(-c.getCost());
                c.complete();
            }
        }
    }

    @Override
    public void addContract(Contract c) {
        contracts.add(c);
    }
}
