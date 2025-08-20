package core;

import UI.Location;

import java.util.HashMap;

import static core.GameConstants.*;

public class Grid extends HashMap<Location, Cell> {

    public Grid(int size) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Cell cell = new Cell(i, j, 0, INITIAL_CELL_UNIT_SPACE, INITIAL_CELL_BUILDING_SPACE);
                put(cell.getLocation(), cell);
            }
        }

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Cell cell = get(new Location(x, y, 0));
                if(x > 0)
                    cell.link(get(new Location(x - 1, y, 0)));
                if(y > 0)
                    cell.link(get(new Location(x, y - 1, 0)));
                if(x < size - 1)
                    cell.link(get(new Location(x + 1, y, 0)));
                if(y < size - 1)
                    cell.link(get(new Location(x, y + 1, 0)));
            }
        }
    }

    public Cell get(Object key) {
        if(containsKey(key))
            return super.get(key);
        else
            throw new IllegalArgumentException("Key not found: " + key);
    }

    public void cycle(int cycle) {
        for(Cell cell : values())
            cell.cycle(cycle);
    }
}
