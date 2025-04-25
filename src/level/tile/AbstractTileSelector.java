package level.tile;

import foundation.Deletable;
import foundation.input.ButtonOrder;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.HexagonalDirection;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import foundation.math.RandomType;
import level.AbstractLevel;
import level.AbstractLevelRenderer;
import render.Renderable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static foundation.input.InputType.*;

public abstract class AbstractTileSelector<T extends AbstractLevel<?, ?>> implements RegisteredButtonInputReceiver, Deletable {
    protected Tile selectedTile = null;
    public Tile mouseOverTile = null;
    protected T level;
    public final Tile[][] tiles;
    public final HashSet<Tile> tileSet = new HashSet<>();
    public final int w, h;

    public AbstractTileSelector(T level) {
        this.level = level;
        tiles = level.tiles;
        w = tiles.length;
        h = tiles[0].length;
        for (Tile[] tileRow : tiles) {
            tileSet.addAll(Arrays.asList(tileRow));
        }
    }

    public void forEachTilesInRadius(Point origin, int radius, BiConsumer<TileSet, Integer> action) {
        TileSet set = new TileSet(w, h);
        set.add(origin);
        action.accept(set, 0);
        for (int i = 0; i < radius; i++) {
            TileSet newTiles = new TileSet(w, h);
            for (Point tile : set) {
                for (HexagonalDirection d : HexagonalDirection.values()) {
                    Point p = d.offset(tile);
                    newTiles.add(p);
                }
            }
            newTiles.removeAll(set);
            action.accept(newTiles, i + 1);
            set.addAll(newTiles);
        }
    }

    public ArrayList<Point> shortestPathTo(Point origin, Point end, TileSet tiles, Function<TileType, Float> tileCostFunction) {
        HashMap<Point, Float> costMap = new HashMap<>();
        HashMap<Point, ArrayList<Point>> pathMap = new HashMap<>();
        tiles.forEach(t -> costMap.put(t, -1f));
        tiles.forEach(t -> pathMap.put(t, new ArrayList<>()));
        costMap.put(origin, 0f);
        HashSet<Point> front = new HashSet<>();
        front.add(origin);
        while (true) {
            HashSet<Point> newFront = new HashSet<>();
            front.forEach(tile -> {
                float cost = costMap.get(tile);
                ArrayList<Point> path = pathMap.get(tile);
                for (HexagonalDirection d : HexagonalDirection.values()) {
                    Point p = d.offset(tile);
                    if (!tiles.contains(p))
                        continue;
                    float newCost = cost + tileCostFunction.apply(getTile(p).type);
                    if (costMap.get(p) == -1 || costMap.get(p) > newCost) {
                        costMap.put(p, newCost);
                        ArrayList<Point> newPath = new ArrayList<>(path);
                        newPath.add(p);
                        pathMap.put(p, newPath);
                        newFront.add(p);
                    }
                }
            });
            if (newFront.isEmpty())
                break;
            front = newFront;
        }
        return pathMap.get(end);
    }

    public HashMap<Point, Float> tilesInRangeCostMap(Point origin, TileSet tiles, Function<TileType, Float> tileCostFunction, float maxCost) {
        HashMap<Point, Float> costMap = new HashMap<>(), finalMap = new HashMap<>();
        tiles.forEach(t -> costMap.put(t, -1f));
        costMap.put(origin, 0f);
        HashSet<Point> front = new HashSet<>();
        front.add(origin);
        while (true) {
            HashSet<Point> newFront = new HashSet<>();
            front.forEach(tile -> {
                float cost = costMap.get(tile);
                for (HexagonalDirection d : HexagonalDirection.values()) {
                    Point p = d.offset(tile);
                    if (!tiles.contains(p))
                        continue;
                    float newCost = cost + tileCostFunction.apply(getTile(p).type);
                    if (newCost > maxCost)
                        continue;
                    if (costMap.get(p) == -1 || costMap.get(p) > newCost) {
                        costMap.put(p, newCost);
                        newFront.add(p);
                    }
                }
            });
            if (newFront.isEmpty())
                break;
            front = newFront;
        }
        costMap.forEach((t, cost) -> {
            if (cost != -1)
                finalMap.put(t, cost);
        });
        return finalMap;
    }

    public boolean validCoordinate(int x, int y) {
        return x >= 0 && y >= 0 && x < tiles.length && y < tiles[0].length;
    }

    public boolean validCoordinate(Point pos) {
        return pos.x >= 0 && pos.y >= 0 && pos.x < tiles.length && pos.y < tiles[0].length;
    }

    public static boolean validCoordinate(int width, int height, Point pos) {
        return pos.x >= 0 && pos.y >= 0 && pos.x < width && pos.y < height;
    }

    @Override
    public boolean blocking(InputType type) {
        return false;
    }

    @Override
    public boolean posInside(ObjPos pos, InputType type) {
        for (Tile[] tileRow : tiles) {
            for (Tile tile : tileRow) {
                if (tile.posInside(pos))
                    return true;
            }
        }
        return false;
    }

    public Tile tileAtSelectablePos(ObjPos pos) {
        return tileAtPos(pos);
    }

    public Tile tileAtPos(ObjPos pos) {
        for (Tile tile : tileSet) {
            if (tile.posInside(pos)) {
                return tile;
            }
        }
        return null;
    }

    public Tile getSelectedTile() {
        return selectedTile;
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return ButtonOrder.HEXAGON;
    }

    @Override
    public void delete() {
        level = null;
    }

    public Tile getTile(Point pos) {
        return tiles[pos.x][pos.y];
    }

    public Tile getTile(int x, int y) {
        return tiles[x][y];
    }

    public void deselect() {
        selectedTile = null;
    }

    public void select(Tile tile) {
        if (tile == null) {
            deselect();
            return;
        }
        selectedTile = tile;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (type == MOUSE_RIGHT) {
            if (!blocked)
                level.levelRenderer.moveCameraStart();
        } else if (type == MOUSE_LEFT) {
            if (!blocked && !level.levelRenderer.runningAnim())
                mouseLeft(pos, inside, type);
        } else if (type == MOUSE_OVER) {
            if (!blocked) {
                ObjPos blockPos = level.levelRenderer.transformCameraPosToBlock(pos);
                level.levelRenderer.moveCameraDown = blockPos.y < AbstractLevelRenderer.MOUSE_EDGE_CAMERA_BORDER;
                level.levelRenderer.moveCameraUp = blockPos.y > Renderable.top() - AbstractLevelRenderer.MOUSE_EDGE_CAMERA_BORDER;
                level.levelRenderer.moveCameraLeft = blockPos.x < AbstractLevelRenderer.MOUSE_EDGE_CAMERA_BORDER;
                level.levelRenderer.moveCameraRight = blockPos.x > Renderable.right() - AbstractLevelRenderer.MOUSE_EDGE_CAMERA_BORDER;
                mouseOverTile = tileAtSelectablePos(pos);
            } else
                mouseOverTile = null;
        } else if (type == ESCAPE) {
            if (!blocked) {
                escapePressed();
            }
        } else if (!blocked && type.isCharInput && type.c == 'm') {
            mKeyPressed();
        }
    }

    protected void mouseLeft(ObjPos pos, boolean inside, InputType type) {
        Tile newTile = tileAtSelectablePos(pos);
        if (newTile == getSelectedTile() && newTile != null && allowDoubleClickToMoveCamera()) {
            level.levelRenderer.setCameraInterpBlockPos(newTile.renderPosCentered);
        } else
            select(newTile);
    }

    protected boolean allowDoubleClickToMoveCamera() {
        return true;
    }

    protected void escapePressed() {
    }

    protected void mKeyPressed() {
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (type == InputType.MOUSE_RIGHT) {
            level.levelRenderer.moveCameraEnd();
        }
    }
}
