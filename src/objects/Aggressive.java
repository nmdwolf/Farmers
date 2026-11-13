package objects;

public interface Aggressive extends Operational {

    int getAttack();

    int getAttackCost();

    int getRange();

    void attack(GameObject obj);

    void changeAttack(int amount);

    void changeRange(int amount);

}
