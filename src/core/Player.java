package core;

import core.GameConstants;

import java.awt.*;
import java.util.HashMap;

public class Player {

    public final static int POPULATION = 100;

    public final static int START_FOOD = 300;
    public final static int START_WOOD = 100;
    public final static int START_WATER = 100;
    public final static int START_STONE = 50;
    public final static int START_IRON = 0;
    public final static int START_COAL = 10;

    private String name;
    private Color color, alternativeColor;

    private int viewLevel;

    private HashMap<Integer, Integer> resources;

    public Player(String name, Color color, Color alternativeColor) {
        this.name = name;
        this.color = color;
        this.alternativeColor = alternativeColor;
        viewLevel = 0;

        resources = new HashMap<>();
        resources.put(GameConstants.FOOD, START_FOOD);
        resources.put(GameConstants.WOOD, START_WOOD);
        resources.put(GameConstants.WATER, START_WATER);
        resources.put(GameConstants.STONE, START_STONE);
        resources.put(GameConstants.COAL, START_COAL);
        resources.put(GameConstants.IRON, START_IRON);

    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public Color getAlternativeColor() {
        return alternativeColor;
    }

    public void changeViewLevel(int amount) { viewLevel += amount; }

    public int getViewLevel() { return viewLevel; }

    public int getResource(int type) {
        return resources.get(type);
    }

    public void changeResource(int type, int amount) {
        resources.put(type, resources.get(type) + amount);
    }
}
