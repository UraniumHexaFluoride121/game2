package unit.bot;

import foundation.Deletable;
import level.Level;
import level.tile.Tile;
import render.Renderable;

import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class BotTileData implements Deletable, Renderable {
    public float[][] tiles;
    public Level level;

    public BotTileData(Level level) {
        this.level = level;
        tiles = new float[level.tilesX][];
        for (int x = 0; x < level.tilesX; x++) {
            tiles[x] = new float[level.tilesY];
            for (int y = 0; y < level.tilesY; y++) {
                tiles[x][y] = 0;
            }
        }
    }

    public BotTileData(BotTileData other) {
        level = other.level;
        tiles = new float[level.tilesX][];
        for (int x = 0; x < level.tilesX; x++) {
            tiles[x] = new float[level.tilesY];
            System.arraycopy(other.tiles[x], 0, tiles[x], 0, level.tilesY);
        }
    }

    public void addValue(Point p, int maxRadius, Function<Integer, Float> valueAtDistance) {
        level.tileSelector.forEachTilesInRadius(p, maxRadius, (points, r) -> {
            float v = valueAtDistance.apply(r);
            for (Point point : points) {
                addPointValue(point, v);
            }
        });
    }

    public void add(BotTileData other, float factor) {
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                tiles[x][y] += other.tiles[x][y] * factor;
            }
        }
    }

    public void apply(BotTileData other, BinaryOperator<Float> op) {
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                tiles[x][y] = op.apply(tiles[x][y], other.tiles[x][y]);
            }
        }
    }

    public void apply(UnaryOperator<Float> op) {
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                tiles[x][y] = op.apply(tiles[x][y]);
            }
        }
    }

    public void addPointValue(Point p, float value) {
        if (level.tileSelector.validCoordinate(p))
            tiles[p.x][p.y] += value;
    }

    public void resetTiles() {
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                tiles[x][y] = 0;
            }
        }
    }

    public void forEachTile(BiConsumer<Point, Float> action) {
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                action.accept(new Point(x, y), tiles[x][y]);
            }
        }
    }

    public float get(Point p) {
        return tiles[p.x][p.y];
    }

    @Override
    public synchronized void render(Graphics2D g) {
        if (level == null)
            return;
        forEachTile((p, v) -> {
            level.getTile(p).renderTile(g, new Color(v > 0 ? 0 : 255, v > 0 ? 255 : 0, 0, Math.clamp((int) Math.abs(v), 0, 255)), Tile.HIGHLIGHT_RENDERER);
        });
    }

    @Override
    public synchronized void delete() {
        level = null;
    }
}
