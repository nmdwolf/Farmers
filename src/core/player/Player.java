package core.player;

import core.InternalSettings;
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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Player {

    private final Color color, altColor;
    private Cell viewpoint;
    private final MissionArchive missionArchive;
    private final AwardArchive awardArchive;

    private final Set<GameObject<?>> objects;
    private Set<GameObject<?>> newObjects, removableObjects;
    private final HashSet<Upgrade> enabledUpgrades;
    private final HashMap<Cell, Integer> visited, spotted;
    private final HashSet<Player> allies;
    private final ResourceContainer resources, totalResources, gained, spent;

    private int pop, popCap, cycle;
    private boolean viewLocked;
    private final Civilization civ;
    private final Memory memory;

    public Player(Memory memory, Color color, Color alternativeColor, Cell start) {
        this.memory = memory;
        this.color = color;
        cycle = InternalSettings.START_CYCLE;
        missionArchive = new MissionArchive(this);
        awardArchive = new AwardArchive(this);
        civ = new Nomads();
        altColor = alternativeColor;
        popCap = InternalSettings.START_POP_CAP;
        viewLocked = true;
        viewpoint = start;
        objects = ConcurrentHashMap.newKeySet();
        newObjects = ConcurrentHashMap.newKeySet();
        removableObjects = ConcurrentHashMap.newKeySet();
        enabledUpgrades = new HashSet<>();
        visited = new HashMap<>();
        spotted = new HashMap<>();
        allies = new HashSet<>();

        resources = new ResourceContainer();
        resources.put("Food", InternalSettings.START_FOOD);
        resources.put("Water", InternalSettings.START_WATER);
        resources.put("Wood", InternalSettings.START_WOOD);
        resources.put("Stone", InternalSettings.START_STONE);
        resources.put("Coal", InternalSettings.START_COAL);
        resources.put("Iron", InternalSettings.START_IRON);

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

            if (object.getType() == InternalSettings.UNIT_TYPE)
                changePop(object.getSize());

            // Change popcap
            object.getLoadout(Spacer.class).ifPresent(spacer -> changePopCap(spacer.getSpaceBoost()));
            object.getLoadout(Spacer.class).ifPresent(spacer -> cell.changeUnitSpace(spacer.getSpaceBoost()));

            Cell loc = object.getCell();
            visit(loc);
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
        if(object.getType() == InternalSettings.UNIT_TYPE)
            changePop(-object.getSize());

        object.getCell().removeContent(object);
        removableObjects.add(object);
    }

    public String getName() {
        return memory.getName();
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

    /**
     * Gives the amount of the specified resource that this player has collected throughout the game.
     * @param type resource type
     * @return amount gained
     */
    public int getGained(String type) {
        return gained.get(type);
    }

    /**
     * Gives the amount of the specified resource that this player has spent throughout the game.
     * @param type resource type
     * @return amount spent
     */
    public int getSpent(String type) {
        return spent.get(type);
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

    public ResourceContainer getGained() {
        return gained;
    }

    public ResourceContainer getSpent() {
        return spent;
    }

    public void cycle() {
        cycle++;
        visited.replaceAll((key, value) -> value + 1);
        var removables = visited.keySet().stream()
                .filter(key -> visited.get(key) >= InternalSettings.FORGET_CELL_THRESHOLD
                        && key.getObjects().stream().map(GameObject::getPlayer).noneMatch(p -> p.equals(this)))
                .toList();
        removables.forEach(key -> {
                    visited.remove(key);
                    spotted.put(key, cycle);
                });
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

    public boolean hasSpotted(Cell loc) { return visited.containsKey(loc) || spotted.containsKey(loc); }

    public boolean hasVisited(Cell loc) { return visited.containsKey(loc); }

    public void spot(Cell cell) {
        if(!visited.containsKey(cell))
            spotted.put(cell, cycle);
    }

    public void visit(Cell cell) {
        spotted.remove(cell);
        visited.put(cell, cycle);
    }

    public Civilization getCivilization() { return civ; }

    public AwardArchive getAwardArchive() { return awardArchive; }

    public List<String> getMessages() {
        return awardArchive.getNewAwards();
    }

    public MissionArchive getMissionArchive() {
        return missionArchive;
    }

    public Memory getMemory() {
        return memory;
    }
}
