package objects.loadouts;

import com.fasterxml.jackson.annotation.*;
import core.OperationCode;
import core.OperationsList;
import core.contracts.Contract;
import objects.Aggressive;
import objects.GameObject;
import objects.units.Warrior;

import java.util.ArrayList;

public class Fighter extends Loadout implements Aggressive {

    private int attack, attackCost, range;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Fighter(@JsonProperty(required = true, value = "attack") int attack, @JsonProperty(value = "attackCost") int attackCost, @JsonProperty(value = "range") int range) {
        super("fighter");
        this.attack = attack;
        this.attackCost = attackCost;
        this.range = range;
    }

    @Override
    public int getRange() {
        return range;
    }

    @Override
    public int getAttack() { return attack; }

    @Override
    public int getAttackCost() {
        return attackCost;
    }

    @Override
    public void attack(GameObject object) { object.changeHealth(-attack); }

    @Override
    public void changeAttack(int amount) { attack += amount; }

    @Override
    public void changeRange(int amount) { range += amount; }

    @Override
    public String toString() {
        return "Attack: " + attack +
                "\nAttack cost: " + attackCost +
                ((range > 0) ? "\nRange: " + range : "");
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        return null;
    }

    @Override
    public void work() {

    }

    @Override
    public void addContract(Contract c) {

    }

    @Override
    public void transferContract(Contract c) {

    }

    @Override
    public ArrayList<Contract> getContracts() {
        return null;
    }

    @Override
    public void seizeActions() {

    }
}
