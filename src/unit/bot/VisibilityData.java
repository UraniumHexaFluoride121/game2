package unit.bot;

import unit.Unit;

import java.awt.*;
import java.util.HashSet;

public record VisibilityData(HashSet<Point> visibleTiles, HashSet<Unit> stealthVisibleUnit) {
}
