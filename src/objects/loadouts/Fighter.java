package objects.loadouts;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import objects.Aggressive;
import objects.GameObject;

import java.io.File;
import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Fighter extends Loadout implements Aggressive {

    private int attack, attackCost;

    @JsonSetter(nulls = Nulls.SKIP)
    private int range = 0;

    private Fighter(String type, int attack, int attackCost, int range) {
        super(type);
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
    public void attack(GameObject object) { object.changeHealth(attack); }

    @Override
    public void changeAttack(int amount) { attack += amount; }

    @Override
    public String toString() {
        return "Attack: " + attack +
                "\nAttack cost: " + attackCost +
                ((range > 0) ? "\nRange: " + range : "");
    }

    public static Fighter createFighter(String className, String group) throws IllegalArgumentException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));

        if("Warrior".equals(group)) {
            File file = new File("src/data/Warriors.json");
            try {
                Fighter[] warriors = mapper.readValue(file, Fighter[].class);
                for(Fighter f : warriors)
                    if(f.getName().equals(className))
                        return f;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            File file = new File("src/data/" + className + ".json");
            try {
                return mapper.readValue(file, Fighter.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalArgumentException("The provided class " + className + " is unknown.");
    }

}
