package objects.loadouts;

import com.fasterxml.jackson.annotation.*;
import objects.GameObject;

public class Fighter extends Loadout {

    private int attack, attackCost, range;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Fighter(@JsonProperty(required = true, value = "attack") int attack,
                   @JsonProperty(value = "attackCost") int attackCost,
                   @JsonProperty(value = "range") int range) {
        super("fighter");
        this.attack = attack;
        this.attackCost = attackCost;
        this.range = range;
    }

    public int getRange() {
        return range;
    }

    public int getAttack() { return attack; }

    public int getAttackCost() {
        return attackCost;
    }

    public void attack(GameObject object) { object.changeHealth(-attack); }

    public void changeAttack(int amount) { attack += amount; }

    public void changeRange(int amount) { range += amount; }

    @Override
    public String toString() {
        return "Attack: " + attack +
                "\nAttack cost: " + attackCost +
                ((range > 0) ? "\nRange: " + range : "");
    }
}
