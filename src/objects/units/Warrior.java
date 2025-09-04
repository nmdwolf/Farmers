package objects.units;

import core.Cell;
import core.OperationCode;
import core.OperationsList;
import core.player.Player;
import core.resources.ResourceContainer;
import objects.Aggressive;
import objects.GameObject;

public class Warrior extends Unit implements Aggressive {

    public Warrior(int animationDelay, int size, int sight, int health, int degradeTime, int degradeAmount, int cycleLength, int energy, ResourceContainer cost) {
        super(animationDelay, size, sight, health, degradeTime, degradeAmount, cycleLength, energy, cost);
    }

    @Override
    public int getAttack() {
        return 0;
    }

    @Override
    public void attack(GameObject obj) {

    }

    @Override
    public void changeAttack(int amount) {

    }

    @Override
    public String getClassLabel() {
        return "";
    }

    @Override
    public String getToken() {
        return "";
    }

    @Override
    public OperationsList getOperations(int cycle, OperationCode code) {
        return null;
    }
}
