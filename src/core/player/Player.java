package core.player;

import core.Cell;
import core.resources.ResourceContainer;
import objects.GameObject;
import core.upgrade.Nomads;
import core.upgrade.Upgrade;
import objects.buildings.Building;
import objects.buildings.Foundation;
import objects.loadouts.Spacer;
import objects.units.Unit;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static core.GameConstants.*;

public class Player {

    private final String name;
    private final Color color, altColor;
    private Cell viewpoint;
    private final MissionArchive missionArchive;
    private final AwardArchive awardArchive;

    private final Set<GameObject<?>> objects;
    private Set<GameObject<?>> newObjects, removableObjects;
    private final HashSet<Upgrade> enabledUpgrades;
    private final HashSet<Cell> discovered, spotted;
    private final HashSet<Player> allies;
    private final ResourceContainer resources, totalResources, gained, spent;

    private int pop, popCap, cycle;
    private boolean viewLocked;
    private final Civilization civ;

    public Player(String name, Color color, Color alternativeColor, Cell start) {
        this.name = name;
        this.color = color;
        cycle = START_CYCLE;
        missionArchive = new MissionArchive(this);
        awardArchive = new AwardArchive(this);
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
        allies = new HashSet<>();

        resources = new ResourceContainer();
        resources.put("Food", START_FOOD);
        resources.put("Water", START_WATER);
        resources.put("Wood", START_WOOD);
        resources.put("Stone", START_STONE);
        resources.put("Coal", START_COAL);
        resources.put("Iron", START_IRON);

        totalResources = new ResourceContainer(resources); // TODO What is this variable used for?
        spent = new ResourceContainer();
        gained = new ResourceContainer();
    }

    /**
     * Gives the set of allies of this {@code Player}.
     * @return set of allies
     */
    public HashSet<Player> getAllies() {
        return allies;
    }

    /**
     * Adds an ally to this {@code Player}.
     * @param p new ally
     */
    public void addAlly(Player p) {
        allies.add(p);
    }

    public Set<GameObject<?>> getObjects() {
        return objects;
    }

    public Set<GameObject<?>> getNewObjects() {
        objects.addAll(newObjects);
        HashSet<GameObject<?>> temp = new HashSet<>(newObjects);
        newObjects = ConcurrentHashMap.newKeySet();
        return temp;
    }

    public Set<GameObject<?>> getRemovableObjects() {
        objects.removeAll(removableObjects);
        HashSet<GameObject<?>> temp = new HashSet<>(removableObjects);
        removableObjects = ConcurrentHashMap.newKeySet();
        return temp;
    }

    /**
     * Make new object ready to be added to the game field.
     * @param object GameObject to be added
     */
    public boolean addObject(GameObject<?> object, Cell cell) {
        object = civ.initObject(object);

        boolean added = false;

        if((object instanceof Unit) && (cell.getUnitAvailable() >= object.getSize()))
            added = true;
        else if((object instanceof Building || object instanceof Foundation<?>) && (cell.getBuildingAvailable() >= object.getSize()))
            added = true;

        if(added) {
            object.initialize(this, cycle);
            object.setCell(cell);

            if (object.getType() == UNIT_TYPE)
                changePop(object.getSize());

            // Change popcap
            object.getLoadout(Spacer.class).ifPresent(spacer -> changePopCap(spacer.getSpaceBoost()));
            object.getLoadout(Spacer.class).ifPresent(spacer -> cell.changeUnitSpace(spacer.getSpaceBoost()));

            Cell loc = object.getCell();
            discover(loc);
            spot(loc.fetch(1, 0, 0));
            spot(loc.fetch(-1, 0, 0));
            spot(loc.fetch(0, 1, 0));
            spot(loc.fetch(0, -1, 0));
            spot(loc.fetch(0, 0, 1));
            spot(loc.fetch(0, 0, -1));

            for (Upgrade upgrade : enabledUpgrades)
                upgrade.apply(object);

            newObjects.add(object);
        }

        return added;
    }

    public void removeObject(GameObject<?> object) {

        // change popcap
        object.getLoadout(Spacer.class).ifPresent(spacer -> changePopCap(-spacer.getSpaceBoost()));
        object.getLoadout(Spacer.class).ifPresent(spacer -> object.getCell().changeUnitSpace(-spacer.getSpaceBoost()));
        if(object.getType() == UNIT_TYPE)
            changePop(-object.getSize());

        object.getCell().removeContent(object);
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

    public int getResource(String type) {
        return resources.get(type);
    }

    public int getTotalResource(String type) {
        return totalResources.get(type);
    }

    public ResourceContainer getResources() { return resources; }

    /**
     * Checks if the Player has the required amount of resources.
     * @param res Map with resource-value pairs.
     * @return true if Player has the requested resources
     */
    public boolean hasResources(ResourceContainer res) {
        for(String resource : res.keySet())
            if(!resource.equals("Time") && resources.get(resource) < res.get(resource))
                return false;
        return true;
    }

    public void changeResource(String type, int amount) {
        resources.add(type, amount);
        totalResources.add(type, amount);

        if(amount > 0)
            gained.add(type, amount);
        else if(amount < 0)
            spent.add(type, -amount);

        awardArchive.validate();
    }

    public void changeResources(ResourceContainer res) {
        for(String resource : res.keySet())
            if(!resource.equals("Time") && res.get(resource) != 0)
                changeResource(resource, res.get(resource));
    }

    public int getGainedAmount(String type) {
        return gained.get(type);
    }

    public int getSpentAmount(String type) {
        return spent.get(type);
    }

    public void cycle() {
        cycle++;
    }

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

    public Set<String> getMessages() {
        Set<String> messages = awardArchive.getNewAwards();
        return messages;
    }

    public AwardArchive getAwardArchive() { return awardArchive; }

    public Civilization getCivilization() { return civ; }

    public void validateMissions() { missionArchive.validate(); }

    public String getCurrentMission() { return missionArchive.getNextDescription(); }
}
