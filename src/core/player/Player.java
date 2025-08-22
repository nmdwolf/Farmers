package core.player;

import core.Cell;
import core.GameConstants;
import objects.resources.Resource;
import objects.resources.ResourceContainer;
import objects.GameObject;
import core.upgrade.Nomads;
import core.upgrade.Upgrade;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static core.GameConstants.*;
import static objects.resources.Resource.*;

public class Player {

    private final String name;
    private final Color color, altColor;
    private Cell viewpoint;
    private final AwardSystem awards;
    private final MissionArchive archive;

    private final Set<GameObject> objects;
    private Set<GameObject> newObjects, removableObjects;
    private final HashSet<Upgrade> enabledUpgrades;
    private final HashSet<Cell> discovered, spotted;
    private final ResourceContainer resources, totalResources;

    private int pop, popCap, cycle;
    private boolean viewLocked;
    private final Civilization civ;

    public Player(String name, Color color, Color alternativeColor, Cell start) {
        this.name = name;
        this.color = color;
        awards = new AwardSystem();
        archive = new MissionArchive();
        civ = new Nomads();
        altColor = alternativeColor;
        popCap = START_POP_CAP;
        viewLocked = true;
        viewpoint = start;
        objects = ConcurrentHashMap.newKeySet();
        newObjects = ConcurrentHashMap.newKeySet();
        removableObjects = ConcurrentHashMap.newKeySet();
        enabledUpgrades = new HashSet<>();
        discovered = new HashSet<>();
        spotted = new HashSet<>();

        resources = new ResourceContainer();
        resources.put(FOOD, GameConstants.START_FOOD);
        resources.put(WOOD, GameConstants.START_WOOD);
        resources.put(WATER, GameConstants.START_WATER);
        resources.put(STONE, GameConstants.START_STONE);
        resources.put(COAL, GameConstants.START_COAL);
        resources.put(IRON, GameConstants.START_IRON);
//        resources.put(TIME, 0);

        totalResources = new ResourceContainer(resources);
    }

    public Set<GameObject> getObjects() {
        return objects;
    }

    public Set<GameObject> getNewObjects() {
        objects.addAll(newObjects);
        HashSet<GameObject> temp = new HashSet<>(newObjects);
        newObjects = ConcurrentHashMap.newKeySet();
        return temp;
    }

    public Set<GameObject> getRemovableObjects() {
        objects.removeAll(removableObjects);
        HashSet<GameObject> temp = new HashSet<>(removableObjects);
        removableObjects = ConcurrentHashMap.newKeySet();
        return temp;
    }

    /**
     * Make new object ready to be added to the game field.
     * @param object GameObject to be added
     */
    public void addObject(GameObject object) {
        object = civ.initObject(object);
        newObjects.add(object);

        //awards.enable(object.getAward(Option.CONSTRUCT));

        Cell loc = object.getCell();
        discover(loc);
        spot(loc.fetch(1, 0, 0));
        spot(loc.fetch(-1, 0, 0));
        spot(loc.fetch(0, 1, 0));
        spot(loc.fetch(0, -1, 0));
        spot(loc.fetch(0, 0, 1));
        spot(loc.fetch(0, 0, -1));

        for(Upgrade upgrade : enabledUpgrades)
            upgrade.apply(object);
    }

    public void removeObject(GameObject object) {
        removableObjects.add(object);
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

    public Cell getViewPoint() { return viewpoint; }

    public void changeViewpoint(Cell loc) { viewpoint = loc; }

    public int getResource(Resource type) {
        return resources.get(type);
    }

    public int getTotalResource(Resource type) {
        return totalResources.get(type);
    }

    /**
     * Checks if the Player has the required amount of objects.resources.
     * @param res Map with Resource-value pairs.
     * @return true if Player has the requested objects.resources
     */
    public boolean hasResources(ResourceContainer res) {
        for(Resource resource : res.keySet())
            if(resource != TIME && resources.get(resource) < res.get(resource))
                return false;
        return true;
    }

    public void changeResource(Resource type, int amount) {
        resources.put(type, resources.get(type) + amount);
        totalResources.put(type, totalResources.get(type) + amount);

        if(type == STONE)
            awards.enable(new Award(START_STONE, "You have mined stones for the very first time."));
    }

    public void changeResources(ResourceContainer res) {
        for(Resource resource : res.keySet())
            if(resource != TIME && res.get(resource) != 0)
                changeResource(resource, res.get(resource));
    }

    public void cycle() { cycle++; }

    public int getCycle() { return cycle; }

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

    public boolean hasSpotted(Cell loc) { return discovered.contains(loc) || spotted.contains(loc); }

    public boolean hasDiscovered(Cell loc) { return discovered.contains(loc); }

    public void spot(Cell cell) {
        if(!discovered.contains(cell))
            spotted.add(cell);
    }

    public void discover(Cell cell) {
        spotted.remove(cell);
        discovered.add(cell);
    }

    public boolean hasAward(Award award) { return awards.hasEnabled(award); }

    public void enableAward(Award award) { awards.enable(award); }

    public Set<String> getMessages() {
        Set<String> messages = awards.getNewAwards();
        return messages;
    }

    public Civilization getCivilization() { return civ; }
}
