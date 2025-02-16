package level.tile;

import foundation.Deletable;
import foundation.input.ButtonOrder;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.HexagonalDirection;
import foundation.math.MathUtil;
import foundation.math.ObjPos;
import foundation.math.RandomType;
import level.Level;
import render.Renderable;
import unit.Unit;
import unit.UnitTeam;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;

import static foundation.input.InputType.*;

public class TileSelector implements RegisteredButtonInputReceiver, Deletable {
    public final Tile[][] tiles;
    public final HashSet<Tile> tileSet = new HashSet<>();
    private Tile selectedTile = null;
    public Tile mouseOverTile = null;

    private Level level;

    public TileSelector(Tile[][] tiles, Level level) {
        this.tiles = tiles;
        this.level = level;
        for (Tile[] tileRow : tiles) {
            tileSet.addAll(Arrays.asList(tileRow));
        }
    }

    public HashSet<Point> withEnemyUnits(HashSet<Point> tiles, Unit unit) {
        HashSet<Point> enemyTiles = new HashSet<>();
        tiles.forEach(t -> {
            if (!getTile(t).isFoW && unit.canFireAt(level.getUnit(t)))
                enemyTiles.add(t);
        });
        return enemyTiles;
    }

    public HashSet<Point> allTiles() {
        HashSet<Point> points = new HashSet<>();
        tileSet.forEach(t -> points.add(t.pos));
        return points;
    }

    public HashSet<Point> allTilesOfType(TileType type) {
        HashSet<Point> points = new HashSet<>();
        tileSet.forEach(t -> {
            if (t.type == type)
                points.add(t.pos);
        });
        return points;
    }

    public HashSet<Point> allTilesExceptType(TileType type) {
        HashSet<Point> points = new HashSet<>();
        tileSet.forEach(t -> {
            if (t.type != type)
                points.add(t.pos);
        });
        return points;
    }

    public HashSet<Point> tilesInRadius(Point origin, int radius) {
        HashSet<Point> tileSet = new HashSet<>();
        tileSet.add(origin);
        for (int i = 0; i < radius; i++) {
            HashSet<Point> newTiles = new HashSet<>();
            for (Point tile : tileSet) {
                for (HexagonalDirection d : HexagonalDirection.values()) {
                    Point p = d.offset(tile);
                    if (validCoordinate(p))
                        newTiles.add(p);
                }
            }
            tileSet.addAll(newTiles);
        }
        return tileSet;
    }

    public ArrayList<Point> tilesInRadiusDeterministic(Point origin, int radius) {
        ArrayList<Point> tileSet = new ArrayList<>();
        tileSet.add(origin);
        for (int i = 0; i < radius; i++) {
            ArrayList<Point> newTiles = new ArrayList<>();
            for (Point tile : tileSet) {
                for (HexagonalDirection d : HexagonalDirection.values()) {
                    Point p = d.offset(tile);
                    if (validCoordinate(p))
                        newTiles.add(p);
                }
            }
            tileSet.addAll(newTiles);
        }
        ArrayList<Point> noDuplicates = new ArrayList<>();
        tileSet.forEach(t -> {
            if (!noDuplicates.contains(t))
                noDuplicates.add(t);
        });
        return noDuplicates;
    }

    public HashSet<Point> tilesWithoutEnemies(HashSet<Point> tiles, UnitTeam team) {
        HashSet<Point> newTiles = new HashSet<>();
        for (Point tile : tiles) {
            if (level.getUnit(tile) == null || level.samePlayerTeam(level.getUnit(tile).team, team) || getTile(tile).isFoW)
                newTiles.add(tile);
        }
        return newTiles;
    }

    public ArrayList<Point> shortestPathTo(Point origin, Point end, HashSet<Point> tiles, Function<TileType, Float> tileCostFunction) {
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

    public HashSet<Point> tilesInRange(Point origin, HashSet<Point> tiles, Function<TileType, Float> tileCostFunction, float maxCost) {
        HashMap<Point, Float> costMap = new HashMap<>();
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
        HashSet<Point> tilesInRange = new HashSet<>();
        costMap.forEach((t, cost) -> {
            if (cost != -1)
                tilesInRange.add(t);
        });
        return tilesInRange;
    }

    public HashSet<Point> pointTerrain(float tilesPerPoint, int radius, TileType generateOn, Function<Integer, Float> generationChance) {
        HashSet<Point> available = allTilesOfType(generateOn);
        int pointCount = (int) (available.size() / tilesPerPoint);
        float probability = available.size() / tilesPerPoint - pointCount;
        if (MathUtil.randBoolean(probability, level.random.getDoubleSupplier(RandomType.TILE_GENERATION)))
            pointCount++;
        HashSet<Point> points = new HashSet<>();
        for (int i = 0; i < pointCount; i++) {
            float x = level.random.generateFloat(RandomType.TILE_GENERATION) * tiles.length;
            float y = level.random.generateFloat(RandomType.TILE_GENERATION) * tiles[0].length;
            Point p = new Point((int) x, (int) y);
            if (points.contains(p) || !available.contains(p))
                i--;
            else
                points.add(p);
        }
        HashSet<Point> terrain = new HashSet<>(points), tested = new HashSet<>(points);
        for (int i = 0; i < radius; i++) {
            HashSet<Point> newTerrain = new HashSet<>();
            terrain.forEach(t -> {
                for (HexagonalDirection d : HexagonalDirection.values()) {
                    Point p = d.offset(t);
                    if (validCoordinate(p) && !tested.contains(p) && available.contains(p))
                        newTerrain.add(p);
                }
            });
            tested.addAll(newTerrain);
            for (Point tile : newTerrain) {
                if (level.random.generateFloat(RandomType.TILE_GENERATION) < generationChance.apply(i))
                    terrain.add(tile);
            }
        }
        return terrain;
    }

    public boolean validCoordinate(int x, int y) {
        return x >= 0 && y >= 0 && x < tiles.length && y < tiles[0].length;
    }

    public boolean validCoordinate(Point pos) {
        return pos.x >= 0 && pos.y >= 0 && pos.x < tiles.length && pos.y < tiles[0].length;
    }

    @Override
    public boolean blocking(InputType type) {
        return false;
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return ButtonOrder.HEXAGON;
    }

    @Override
    public boolean posInside(ObjPos pos) {
        for (Tile[] tileRow : tiles) {
            for (Tile tile : tileRow) {
                if (tile.posInside(pos))
                    return true;
            }
        }
        return false;
    }

    public Tile tileAtSelectablePos(ObjPos pos) {
        if (level.hasActiveAction()) {
            for (Point p : level.selectedUnit.selectableTiles()) {
                Tile tile = tiles[p.x][p.y];
                if (tile.posInside(pos)) {
                    return tile;
                }
            }
        } else {
            for (Tile tile : tileSet) {
                if (tile.posInside(pos)) {
                    return tile;
                }
            }
        }
        return null;
    }

    public Tile tileAtPos(ObjPos pos) {
        for (Tile tile : tileSet) {
            if (tile.posInside(pos)) {
                return tile;
            }
        }
        return null;
    }

    public void deselect() {
        selectedTile = null;
        level.levelRenderer.tileInfo.setEnabled(false);
        level.updateSelectedUnit();
    }

    public void select(Tile tile) {
        if (tile == null) {
            deselect();
            return;
        }
        selectedTile = tile;
        level.levelRenderer.tileInfo.setTile(tile);
        level.levelRenderer.tileInfo.setEnabled(true);
    }

    public Tile getSelectedTile() {
        return selectedTile;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (type == MOUSE_RIGHT) {
            if (!blocked)
                level.levelRenderer.moveCameraStart();
        } else if (type == MOUSE_LEFT) {
            if (blocked || level.levelRenderer.runningAnim())
                return;
            if (!level.hasActiveAction()) {
                Tile newTile = tileAtSelectablePos(pos);
                if (newTile == getSelectedTile() && newTile != null) {
                    level.levelRenderer.setCameraInterpBlockPos(newTile.renderPosCentered);
                } else
                    select(newTile);
            } else {
                level.selectedUnit.onTileClicked(tileAtSelectablePos(pos), level.getActiveAction());
            }
            level.updateSelectedUnit();
        } else if (type == MOUSE_OVER) {
            if (!blocked) {
                ObjPos blockPos = level.levelRenderer.transformCameraPosToBlock(pos);
                if (blockPos.y < 1) level.levelRenderer.moveCameraDown = true;
                if (blockPos.y > Renderable.top() - 1) level.levelRenderer.moveCameraUp = true;
                if (blockPos.x < 1) level.levelRenderer.moveCameraLeft = true;
                if (blockPos.x > Renderable.right() - 1) level.levelRenderer.moveCameraRight = true;
                mouseOverTile = tileAtSelectablePos(pos);
            } else
                mouseOverTile = null;
        } else if (type == ESCAPE) {
            if (!blocked) {
                deselectAction();
            }
        }
    }

    public void deselectAction() {
        if (level.hasActiveAction()) {
            if (level.levelRenderer.highlightTileRenderer != null)
                level.levelRenderer.highlightTileRenderer.close();
            level.levelRenderer.unitTileBorderRenderer = null;
            level.setActiveAction(null);
        } else
            deselect();
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (type == InputType.MOUSE_RIGHT) {
            level.levelRenderer.moveCameraEnd();
        }
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
}
