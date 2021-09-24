package items;

import core.*;

import general.*;
import items.upgrade.EvolveUpgrade;
import items.upgrade.Upgrade;

import java.awt.image.BufferedImage;
import java.util.*;

import static core.Option.*;

public abstract class GameObject {

    private final int id;
    private final HashSet<Type> descriptions;
    private final Map<Option, Integer> parameters;

    protected Location location;
    protected Player player;

    public GameObject(Player player, Location location, Map<Option, Integer> params) {
        id = CustomMethods.getNewIdentifier();

        this.location = location;
        this.player = player;

        descriptions = new HashSet<>();

        if(!params.containsKey(SIGHT) || !params.containsKey(SIZE)
                || !params.containsKey(STATUS) || !params.containsKey(MAX_HEALTH)
                || !params.containsKey(DEGRADATION_AMOUNT) || !params.containsKey(DEGRADATION_CYCLE))
            throw new IllegalArgumentException("Missing default parameter");

        parameters = params;
        parameters.put(LEVEL, 0);
        parameters.put(CYCLE, 0);
        parameters.put(TOTAL_CYCLE, 0);
        parameters.put(OLD_STATUS, parameters.get(OLD_STATUS));
        parameters.put(HEALTH, parameters.get(MAX_HEALTH));

        if(params.containsKey(SPACE))
            updateTypes(Type.SPACER);
    }

    public int getObjectIdentifier() { return id; }

    public Location getLocation() { return location; }
    public void setLocation(Location loc) { location = loc; }
    public Player getPlayer() { return player; }

    public Set<Type> getTypes() { return descriptions; }
    public void updateTypes(Type... types) {
        descriptions.addAll(Arrays.asList(types));
    }
    public GameObject castAs(Type description) { return this; }

    public abstract String getClassIdentifier();
    public abstract String getToken();

    public abstract List<Upgrade> getUpgrades();
    public abstract List<GameObject> getProducts();
    public abstract List<EvolveUpgrade> getEvolutions();
    public abstract OperationsList getOperations();

    public BufferedImage getSprite() { return null; }

    // Typed methods

    public void typedDo(Type type, TypedConsumer toRun) throws TypeException {
        if(getTypes().contains(type))
            toRun.accept(this.castAs(type));
    }
    public void perform(Option option) {
        switch(option) {
            case DESTROY:
                player.removeObject(this);
                break;
            case TOTAL_CYCLE:
                parameters.put(TOTAL_CYCLE, parameters.get(TOTAL_CYCLE) + 1);
                parameters.put(CYCLE, parameters.get(CYCLE) + 1);
                break;
            case DEGRADE:
                if(parameters.get(CYCLE) >= parameters.get(DEGRADATION_CYCLE))
                    changeValue(HEALTH, -parameters.get(DEGRADATION_AMOUNT));
                if(getValue(HEALTH) <= 0) {
                    perform(DESTROY);
                }
                break;
        }

    }
    public boolean checkStatus(Option option) { return (option == DESTROY && getValue(HEALTH) <= 0); }
    public int getValue(Option option) {
        Integer param = parameters.get(option);
        return param == null ? -1 : param;
    }
    public void changeValue(Option option, int amount) {
        switch(option) {
            case STATUS: case OLD_STATUS: case MAX_HEALTH:
                setValue(option, amount);
                break;
            default:
                if(parameters.containsKey(option))
                    parameters.put(option, parameters.get(option) + amount);
                else
                    setValue(option, amount);
                break;
        }
    }
    public void setValue(Option option, int amount) {
        switch(option) {
            case STATUS:
                parameters.put(OLD_STATUS, parameters.get(STATUS));
                parameters.put(STATUS, amount);
                break;
            case MAX_HEALTH:
                parameters.put(MAX_HEALTH, amount);
                parameters.put(HEALTH, amount);
                break;
            default:
                parameters.put(option, amount);
                break;
        }
    }
    public ResourceContainer getResources(Option option) { return ResourceContainer.EMPTY_CONTAINER; }

    // BASE METHODS

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GameObject) {
            return id == ((GameObject) obj).getObjectIdentifier();
        }
        return false;
    }
}
