package objects;

public interface Aggressive {

    int getAttack();

    int getAttackCost();

    int getRange();

    void attack(GameObject<?> obj);

    void changeAttack(int amount);

}
