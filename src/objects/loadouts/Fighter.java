package objects.loadouts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import objects.Aggressive;
import objects.GameObject;
import objects.units.Warrior;

import java.io.File;
import java.io.IOException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Fighter extends Loadout implements Aggressive {

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

    @Override
    public String toString() {
        return "Attack: " + attack +
                "\nAttack cost: " + attackCost;
    }

    public static Fighter createFighter(String className, String group) throws IllegalArgumentException {
        ObjectMapper mapper = new ObjectMapper();
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
        throw new IllegalArgumentException("The provided class " + className + "is unknown.");
    }

}
