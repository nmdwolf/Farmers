package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import objects.Aggressive;
import objects.GameObject;
import objects.Operational;

import java.io.File;
import java.io.IOException;

public class Fighter<T extends GameObject & Aggressive & Operational> extends Loadout<T> implements Aggressive {

    private int attack, attackCost;

    @JsonCreator
    public Fighter(@JsonProperty("name") String name, @JsonProperty("attack") int attack, @JsonProperty("attackCost") int attackCost) {
        super(name);
        this.attack = attack;
        this.attackCost = attackCost;
    }

    @Override
    public int getAttack() { return attack; }

    @Override
    public void attack(GameObject object) { object.changeHealth(attack); }

    @Override
    public void changeAttack(int amount) { attack += amount; }

    @Override
    public int getEnergyCost() {
        return attackCost;
    }

    public static Fighter<?> createFighter(String className) {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/data/" + className + ".json");
        try {
            return (Fighter<?>)mapper.readValue(file, Fighter.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "Attack: " + attack +
                "\nAttack cost: " + attackCost;
    }
}
