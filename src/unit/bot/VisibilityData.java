package unit.bot;

import level.tile.TileSet;
import unit.Unit;

import java.awt.*;
import java.util.HashSet;

public record VisibilityData(TileSet visibleTiles, HashSet<Unit> stealthVisibleUnit) {
}
