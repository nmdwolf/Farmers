package objects.loadouts;

import com.fasterxml.jackson.annotation.*;
import objects.Aggressive;
import objects.GameObject;
import objects.templates.FighterTemplate;

public class Fighter extends Loadout<FighterTemplate> implements Aggressive {

    private int attack, attackCost, range;

    public Fighter(FighterTemplate temp) {
        super(temp);
        this.attack = temp.attack;
        this.attackCost = temp.attackCost;
        this.range = temp.range;
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
    public String toString() {
        return "Attack: " + attack +
                "\nAttack cost: " + attackCost +
                ((range > 0) ? "\nRange: " + range : "");
    }

}
