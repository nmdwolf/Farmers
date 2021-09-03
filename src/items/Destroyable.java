package items;

public interface Destroyable extends GameObject {

    public int getHealth();

    public int getMaxHealth();

    public void changeHealth(int amount);
}
