package objects;

public interface Healer {

    void heal(GameObject<?> obj);

    int getHeal();

    void setHeal(int heal);
}
