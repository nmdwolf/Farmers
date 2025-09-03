package objects;

public interface Aggressive {

    int getAttack();

    int getEnergyCost();

    void attack(GameObject obj);

    void changeAttack(int amount);

}
