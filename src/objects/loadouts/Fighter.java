package objects.loadouts;

import core.OperationCode;
import core.OperationsList;
import core.contracts.AttackContract;
import objects.Aggressive;
import objects.GameObject;
import objects.Operational;

import static core.OperationsList.EMPTY_LIST;

public class Fighter<T extends GameObject & Aggressive & Operational> extends Loadout<T> implements Aggressive {

    private int attack, attackCost;

    public Fighter(T object, int attack, int attackCost) {
        super(object);
        this.attack = attack;
        this.attackCost = attackCost;
    }

    @Override
    public int getAttack() { return attack; }

    @Override
    public void attack(GameObject object) { object.changeHealth(attack); }

    @Override
    public void changeAttack(int amount) { attack += amount; }

    // TODO add target (maybe through Property?)
    public OperationsList getOperations(OperationCode code) {
        if(code == OperationCode.ATTACK)
            return new OperationsList("Attack", target -> getOwner().addContract(new AttackContract(getOwner(), getEnergyCost(), target)));
        return EMPTY_LIST;
    }

    @Override
    public int getEnergyCost() {
        return attackCost;
    }
}
