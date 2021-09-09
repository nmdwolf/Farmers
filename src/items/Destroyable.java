package items;

public interface Destroyable extends GameObject {

    int getHealth();

    int getMaxHealth();

    void changeHealth(int amount);

    void changeMaxHealth(int amount);

    int getDegradationStart();

    void changeDegradationStart(int amount);

    void degrade();

    void destroy();
}
