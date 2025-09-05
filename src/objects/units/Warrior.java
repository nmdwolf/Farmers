package objects.units;

import UI.CustomMethods;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ConstructorDetector;
import com.fasterxml.jackson.databind.json.JsonMapper;
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

    private final String className;

    @JsonCreator
    public Warrior(@JsonProperty("animationDelay") int animationDelay, @JsonProperty("size") int size, @JsonProperty("sight") int sight, @JsonProperty("health") int health, @JsonProperty("degradeTime") int degradeTime, @JsonProperty("degradeAmount") int degradeAmount, @JsonProperty("cycleLength") int cycleLength, @JsonProperty("energy") int energy, @JsonProperty("cost") ResourceContainer cost, @JsonProperty("name") String className) {
        super(animationDelay, size, sight, health, degradeTime, degradeAmount, cycleLength, energy, cost);
        addLoadout(Fighter.createFighter(className, "Warrior"));
        this.className = className;
    }

    public static Warrior createWarrior(String className) throws IllegalArgumentException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/data/Warriors.json");
        try {
            Warrior[] warriors = mapper.readValue(file, Warrior[].class);
            for(Warrior w : warriors)
                if(w.getClassLabel().equals(className))
                    return w;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        throw new IllegalArgumentException("The provided class " + className + "is unknown.");
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
        return className;
    }

    @Override
    public String getToken() {
        return className.substring(0, 1);
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        return OperationsList.EMPTY_LIST;
    }

    @Override
    public @NotNull Optional<BufferedImage> getSprite(boolean max) {
        int size = max ? GameConstants.SPRITE_SIZE_MAX : GameConstants.SPRITE_SIZE;
        return CustomMethods.loadSprite(className, size, size);
    }
}
