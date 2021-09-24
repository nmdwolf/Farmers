package core;

import items.GameObject;
import items.upgrade.Upgrade;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static core.GameConstants.*;
import static core.Resource.*;

public class Player {

    private final String name;
    private final Color color, altColor;
    private Location viewpoint;

    private final Set<GameObject> objects;
    private final HashSet<Upgrade> enabledUpgrades;
    private final HashSet<Location> discovered, spotted;
    private final HashMap<Resource, Integer> resources, totalResources;

    private int pop, popCap;
    private boolean viewLocked;
    private final HashSet<String> hasConstructed;

    public Player(String name, Color color, Color alternativeColor) {
        this.name = name;
        this.color = color;
        altColor = alternativeColor;
        popCap = START_POP_CAP;
        viewLocked = true;
        viewpoint = new Location(rand.nextInt(NUMBER_OF_CELLS), rand.nextInt(NUMBER_OF_CELLS), 0);
        objects = ConcurrentHashMap.newKeySet();
        enabledUpgrades = new HashSet<>();
        discovered = new HashSet<>();
        spotted = new HashSet<>();
        hasConstructed = new HashSet<>();

        resources = new HashMap<>();
        resources.put(FOOD, GameConstants.START_FOOD);
        resources.put(WOOD, GameConstants.START_WOOD);
        resources.put(WATER, GameConstants.START_WATER);
        resources.put(STONE, GameConstants.START_STONE);
        resources.put(COAL, GameConstants.START_COAL);
        resources.put(IRON, GameConstants.START_IRON);

        totalResources = new HashMap<>(resources);
    }

    public Set<GameObject> getObjects() {
        return objects;
    }

    public void addObject(GameObject object) {
        objects.add(object);
        hasConstructed.add(object.getToken());

        Location loc = object.getLocation();
        discover(loc);
        spot(new Location(loc.x + 1, loc.y, loc.z));
        spot(new Location(loc.x - 1, loc.y, loc.z));
        spot(new Location(loc.x, loc.y + 1, loc.z));
        spot(new Location(loc.x, loc.y - 1, loc.z));

        for(Upgrade upgrade : enabledUpgrades)
            upgrade.notifyObserver(object);
    }

    public void removeObject(GameObject object) {
        objects.remove(object);
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public Color getAlternativeColor() {
        return altColor;
    }

    public Location getViewPoint() { return viewpoint; }

    public void changeViewpoint(Location loc) { viewpoint = loc; }

    public int getResource(Resource type) {
        return resources.get(type);
    }

    public int getTotalResource(Resource type) {
        return totalResources.get(type);
    }

    /**
     *
     * @param res Map with Resource-value pairs. Every value is assumed to be negative (it represents a cost).
     * @return true if Player has the requested resources
     */
    public boolean hasResources(Map<Resource,Integer> res) {
        for(Resource resource : res.keySet())
            if(resources.get(resource) < -res.get(resource))
                return false;
        return true;
    }

    public void changeResource(Resource type, int amount) {
        resources.put(type, resources.get(type) + amount);
        totalResources.put(type, totalResources.get(type) + amount);
    }

    public void changeResources(Map<Resource,Integer> res) {
        for(Resource resource : res.keySet()) {
            resources.put(resource, resources.get(resource) + res.get(resource));
            totalResources.put(resource, totalResources.get(resource) + res.get(resource));
        }
    }

    public boolean isViewLocked() { return viewLocked; }

    public void unlockView() {
        viewLocked = false;
    }

    public boolean hasUpgrade(Upgrade upgrade) {
        return enabledUpgrades.contains(upgrade);
    }

    public void enableUpgrade(Upgrade upgrade) {
        enabledUpgrades.add(upgrade);
    }

    public int getPop() { return pop; }

    public int getPopCap() { return popCap; }

    public void changePop(int amount) { pop += amount; }

    public void changePopCap(int amount) { popCap += amount; }

    public boolean hasSpotted(Location loc) { return spotted.contains(loc); }

    public void spot(Location loc) {
        if(!discovered.contains(loc))
            spotted.add(loc);
    }

    public boolean hasDiscovered(Location loc) { return discovered.contains(loc); }

    public void discover(Location loc) {
        spotted.remove(loc);
        discovered.add(loc);
    }

    public boolean hasConstructed(String token) {return hasConstructed.contains(token); }
}
