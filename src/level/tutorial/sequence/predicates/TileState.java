package level.tutorial.sequence.predicates;

import level.Level;
import level.tile.Tile;
import unit.Unit;

import java.awt.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public abstract class TileState {
    public static Predicate<Level> tile(BiPredicate<Level, Tile> p, int x, int y) {
        return l -> p.test(l, l.getTile(x, y));
    }

    public static Predicate<Level> anyOf(BiPredicate<Level, Tile> p, Point... points) {
        return l -> {
            for (Point point : points) {
                if (p.test(l, l.getTile(point)))
                    return true;
            }
            return false;
        };
    }

    public static Predicate<Level> allOf(BiPredicate<Level, Tile> p, Point... points) {
        return l -> {
            for (Point point : points) {
                if (!p.test(l, l.getTile(point)))
                    return false;
            }
            return true;
        };
    }

    public static Predicate<Level> any(BiPredicate<Level, Tile> p) {
        return l -> {
            for (Tile tile : l.tileSelector.tileSet) {
                if (p.test(l, tile))
                    return true;
            }
            return false;
        };
    }

    public static Predicate<Level> all(BiPredicate<Level, Tile> p) {
        return l -> {
            for (Tile tile : l.tileSelector.tileSet) {
                if (!p.test(l, tile))
                    return false;
            }
            return true;
        };
    }

    public static BiPredicate<Level, Tile> tileVisible() {
        return (l, t) -> !t.isFoW;
    }

    public static BiPredicate<Level, Tile> hasAnyUnit() {
        return (l, t) -> l.getUnit(t.pos) != null;
    }

    public static BiPredicate<Level, Tile> noUnit() {
        return (l, t) -> l.getUnit(t.pos) == null;
    }

    public static BiPredicate<Level, Tile> hasUnit(Predicate<Unit> filter) {
        return (l, t) -> {
            Unit u = l.getUnit(t.pos);
            return u != null && filter.test(u);
        };
    }
}
