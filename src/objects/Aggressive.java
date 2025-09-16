package objects;

public interface Aggressive<T extends GameObject<T> & Aggressive<T>> {

    int getAttack();

    int getAttackCost();

    int getRange();

    void attack(GameObject<?> obj);

    void changeAttack(int amount);

    void changeRange(int amount);

}
