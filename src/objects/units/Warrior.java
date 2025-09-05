package objects.units;

import UI.CustomMethods;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import core.GameConstants;
import core.OperationCode;
import core.OperationsList;
import core.resources.ResourceContainer;
import objects.Aggressive;
import objects.GameObject;
import objects.loadouts.Fighter;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Warrior extends Unit implements Aggressive {

    private final String type;

    public Warrior(int animationDelay, int size, int sight, int health, int degradeTime, int degradeAmount, int cycleLength, int energy, ResourceContainer cost, String type) {
        super(animationDelay, size, sight, health, degradeTime, degradeAmount, cycleLength, energy, cost);
        addLoadout(Fighter.createFighter(type, "Warrior"));
        this.type = type;
    }

    @Override
    public int getRange() {
        return getLoadout(Fighter.class).map(Fighter::getRange).orElse(0);
    }

    @Override
    public int getAttack() {
        return getLoadout(Fighter.class).map(Fighter::getAttack).orElse(0);
    }

    @Override
    public void attack(GameObject obj) {
        getLoadout(Fighter.class).ifPresent(l -> l.attack(obj));
    }

    @Override
    public void changeAttack(int amount) {
        getLoadout(Fighter.class).ifPresent(l -> l.changeAttack(amount));
    }

    @Override
    public String getClassLabel() {
        return type;
    }

    @Override
    public String getToken() {
        return type.substring(0, 1);
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        return OperationsList.EMPTY_LIST;
    }

    @Override
    public @NotNull Optional<BufferedImage> getSprite(boolean max) {
        int size = max ? GameConstants.SPRITE_SIZE_MAX : GameConstants.SPRITE_SIZE;
        return CustomMethods.loadSprite(type, size, size);
    }

    public static Warrior createWarrior(String className) throws IllegalArgumentException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        File file = new File("src/data/Warriors.json");
        try {
            Warrior[] warriors = mapper.readValue(file, Warrior[].class);
            for(Warrior w : warriors)
                if(w.getClassLabel().equals(className))
                    return w;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        throw new IllegalArgumentException("The provided class " + className + " is unknown.");
    }
}
