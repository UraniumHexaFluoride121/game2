package level.tile;

import foundation.math.HexagonalDirection;
import foundation.math.MathUtil;
import foundation.math.RandomType;
import level.AbstractLevel;
import level.Level;
import unit.Unit;
import unit.UnitTeam;
import unit.bot.VisibilityData;

import java.awt.*;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Predicate;

public class TileModifier {
    private final AbstractLevel<?, ?> l;
    private final AbstractTileSelector<?> t;
    public TileSet set;

    public TileModifier(AbstractLevel<?, ?> l, TileSet set) {
        this.l = l;
        t = l.tileSelector;
        this.set = set;
    }

    public static Predicate<Tile> tileOfType(TileType type) {
        return tile -> tile.type == type;
    }

    public TileModifier tileFilter(Predicate<Tile> predicate) {
        set.removeIf(p -> !predicate.test(t.getTile(p)));
        return this;
    }

    public TileModifier remove(TileSet other) {
        set.removeAll(other);
        return this;
    }

    public TileModifier add(TileSet other) {
        set.addAll(other);
        return this;
    }

    public TileModifier pointTerrain(float tilesPerPoint, int radius, Function<Integer, Float> generationChance) {
        int pointCount = (int) (set.size() / tilesPerPoint);
        float probability = set.size() / tilesPerPoint - pointCount;
        if (MathUtil.randBoolean(probability, l.random.getDoubleSupplier(RandomType.TILE_GENERATION)))
            pointCount++;
        TileSet points = new TileSet(t.w, t.h);
        for (int i = 0; i < pointCount; i++) {
            float x = l.random.generateFloat(RandomType.TILE_GENERATION) * t.w;
            float y = l.random.generateFloat(RandomType.TILE_GENERATION) * t.h;
            Point p = new Point((int) x, (int) y);
            if (points.contains(p) || !set.contains(p))
                i--;
            else
                points.add(p);
        }
        TileSet terrain = points.copy(), tested = points.copy();
        for (int i = 0; i < radius; i++) {
            HashSet<Point> newTerrain = new HashSet<>();
            terrain.forEach(t -> {
                for (HexagonalDirection d : HexagonalDirection.values()) {
                    Point p = d.offset(t);
                    if (!tested.contains(p) && set.contains(p))
                        newTerrain.add(p);
                }
            });
            tested.addAll(newTerrain);
            for (Point tile : newTerrain) {
                if (l.random.generateFloat(RandomType.TILE_GENERATION) < generationChance.apply(i))
                    terrain.add(tile);
            }
        }
        set = terrain;
        return this;
    }

    public TileModifier tilesInRange(Point origin, Function<TileType, Float> tileCostFunction, float maxCost) {
        set = new TileSet(t.w, t.h, new HashSet<>(t.tilesInRangeCostMap(origin, set, tileCostFunction, maxCost).keySet()));
        return this;
    }

    public TileModifier unitFilter(Predicate<Unit> predicate) {
        Level l = (Level) this.l;
        set.removeIf(p -> !predicate.test(l.getUnit(p)));
        return this;
    }

    public static Predicate<Unit> withEnemiesThatCanBeFiredAt(Unit thisUnit, VisibilityData v) {
        return u -> u != null && u.visible(v) && thisUnit.canFireAt(u);
    }

    public static Predicate<Unit> hasAlliedUnit(UnitTeam thisTeam, Level level) {
        return u -> u != null && level.samePlayerTeam(u.team, thisTeam);
    }

    public static Predicate<Unit> withoutVisibleEnemies(UnitTeam thisTeam, Level level) {
        return u -> u == null || level.samePlayerTeam(u.team, thisTeam) || !u.renderVisible();
    }

    public static Predicate<Unit> withVisibleEnemies(UnitTeam thisTeam, Level level) {
        return u -> u != null && !level.samePlayerTeam(u.team, thisTeam) && u.renderVisible();
    }

    public static Predicate<Unit> withoutVisibleEnemies(UnitTeam thisTeam, Level level, VisibilityData v) {
        return u -> u == null || level.samePlayerTeam(u.team, thisTeam) || !u.visible(v);
    }
}
