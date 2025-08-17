package level.tile;

import foundation.math.HexagonalDirection;
import level.AbstractLevel;
import level.Level;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class TileSet implements Collection<Point> {
    public final int width, height;
    public Collection<Point> tiles;

    public TileSet(int width, int height, Collection<Point> tiles) {
        this.width = width;
        this.height = height;
        this.tiles = tiles;
    }

    public TileSet(int width, int height) {
        this(width, height, new HashSet<>());
    }

    public TileSet(AbstractLevel<?, ?> l) {
        this(l, new HashSet<>());
    }

    public TileSet(AbstractLevel<?, ?> l, Collection<Point> tiles) {
        width = l == null ? -1 : l.tilesX;
        height = l == null ? -1 : l.tilesY;
        this.tiles = tiles;
    }

    public static TileSet all(AbstractLevel<?, ?> l) {
        return new TileSet(l, new HashSet<>(l.tileSelector.tileSet.stream().map(t -> t.pos).toList()));
    }

    public static TileSet of(AbstractLevel<?, ?> l, Point... points) {
        return new TileSet(l, List.of(points));
    }

    public static TileSet tilesInRadius(Point origin, int radius, Level l) {
        TileSet set = new TileSet(l);
        set.add(origin);
        for (int i = 0; i < radius; i++) {
            HashSet<Point> newTiles = new HashSet<>();
            for (Point tile : set) {
                for (HexagonalDirection d : HexagonalDirection.values()) {
                    Point p = d.offset(tile);
                    newTiles.add(p);
                }
            }
            set.addAll(newTiles);
        }
        return set;
    }

    public static TileSet tilesInRadius(Point origin, int from, int to, Level l) {
        return tilesInRadius(origin, to, l).m(l, t -> t.remove(tilesInRadius(origin, from - 1, l)));
    }

    public static TileSet tilesInRadiusDeterministic(Point origin, int radius, Level l) {
        TileSet set = new TileSet(l, new ArrayList<>());
        set.add(origin);
        for (int i = 0; i < radius; i++) {
            ArrayList<Point> newTiles = new ArrayList<>();
            for (Point tile : set) {
                for (HexagonalDirection d : HexagonalDirection.values()) {
                    Point p = d.offset(tile);
                    newTiles.add(p);
                }
            }
            set.addAll(newTiles);
        }
        return set;
    }

    public TileSet copy() {
        if (tiles instanceof List<Point>)
            return new TileSet(width, height, new ArrayList<>(tiles));
        else
            return new TileSet(width, height, new HashSet<>(tiles));
    }

    public TileSet m(AbstractLevel<?, ?> level, UnaryOperator<TileModifier> action) {
        return action.apply(new TileModifier(level, this)).set;
    }

    public static Collector<Point, ?, TileSet> toTileSet(int w, int h) {
        return Collector.of(() -> new TileSet(w, h), TileSet::add, (a, b) -> {
            a.addAll(b);
            return a;
        });
    }

    @Override
    public Stream<Point> parallelStream() {
        return tiles.parallelStream();
    }

    @Override
    public Stream<Point> stream() {
        return tiles.stream();
    }

    @Override
    public boolean removeIf(Predicate<? super Point> filter) {
        return tiles.removeIf(filter);
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return tiles.toArray(generator);
    }

    @Override
    public void forEach(Consumer<? super Point> action) {
        tiles.forEach(action);
    }

    @Override
    public int size() {
        return tiles.size();
    }

    @Override
    public Iterator<Point> iterator() {
        return tiles.iterator();
    }

    @Override
    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return tiles.contains(o);
    }

    @Override
    public boolean add(Point point) {
        if (!tiles.contains(point) && (AbstractTileSelector.validCoordinate(width, height, point) || width < 0))
            return tiles.add(point);
        else
            return false;
    }

    @Override
    public boolean remove(Object o) {
        return tiles.remove(o);
    }

    @Override
    public void clear() {
        tiles.clear();
    }

    @Override
    public Object[] toArray() {
        return tiles.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return tiles.toArray(a);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return tiles.retainAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Point> c) {
        boolean modified = false;
        for (Point point : c) {
            if (add(point))
                modified = true;
        }
        return modified;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return tiles.containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return tiles.removeAll(c);
    }
}
