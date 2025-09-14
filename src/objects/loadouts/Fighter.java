package objects.loadouts;

import com.fasterxml.jackson.annotation.*;
import objects.Aggressive;
import objects.GameObject;

public class Fighter extends Loadout implements Aggressive {

    private int attack, attackCost, range;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Fighter(@JsonProperty(required = true) int attack, @JsonProperty(defaultValue = "0") int attackCost, @JsonProperty(defaultValue = "0") int range) {
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
    public void attack(GameObject<?> object) { object.changeHealth(-attack); }

    @Override
    public void changeAttack(int amount) { attack += amount; }

    @Override
    public String toString() {
        return "Attack: " + attack +
                "\nAttack cost: " + attackCost +
                ((range > 0) ? "\nRange: " + range : "");
    }

}
