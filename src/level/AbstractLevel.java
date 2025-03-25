package level;

import foundation.Deletable;
import foundation.input.ButtonRegister;
import foundation.math.ObjPos;
import foundation.math.RandomHandler;
import foundation.tick.RegisteredTickable;
import level.tile.AbstractTileSelector;
import level.tile.Tile;
import level.tile.TileType;
import render.Renderable;
import unit.TileMapDisplayable;
import unit.UnitTeam;

import java.awt.*;
import java.util.HashSet;
import java.util.function.BiConsumer;

public abstract class AbstractLevel<T extends AbstractLevelRenderer<?>, U extends AbstractTileSelector<?>> implements Renderable, Deletable, RegisteredTickable, TileMapDisplayable {
    public ButtonRegister buttonRegister = new ButtonRegister();
    public final int tilesX, tilesY;
    public final Tile[][] tiles;
    public RandomHandler random;
    public long seed;
    public final ObjPos tileBound;
    public T levelRenderer;
    public U tileSelector;

    public AbstractLevel(int tilesX, int tilesY, long seed) {
        this.tilesX = tilesX;
        this.tilesY = tilesY;
        createRandom(seed);
        tiles = new Tile[tilesX][];
        for (int x = 0; x < tilesX; x++) {
            tiles[x] = new Tile[tilesY];
            for (int y = 0; y < tilesY; y++) {
                tiles[x][y] = new Tile(x, y, TileType.EMPTY, this);
            }
        }
        tileBound = Tile.getTilesBound(tilesX, tilesY);
    }

    public void createRandom(long seed) {
        this.seed = seed;
        random = new RandomHandler(seed);
    }

    public void generateTiles() {
        tileSelector.tileSet.forEach(t -> t.setTileType(TileType.EMPTY, this));
        HashSet<Point> nebula = tileSelector.pointTerrain(40, 3, TileType.EMPTY, r -> switch (r) {
            case 0 -> .7f;
            case 1 -> .3f;
            case 2 -> .1f;
            default -> 0f;
        });
        for (Point p : nebula) {
            tileSelector.getTile(p).setTileType(TileType.NEBULA, this);
        }
        HashSet<Point> denseNebula = tileSelector.pointTerrain(10, 1, TileType.NEBULA, r -> switch (r) {
            case 0 -> .1f;
            default -> 0f;
        });
        for (Point p : denseNebula) {
            tileSelector.getTile(p).setTileType(TileType.DENSE_NEBULA, this);
        }
        HashSet<Point> asteroids = tileSelector.pointTerrain(18, 2, TileType.EMPTY, r -> switch (r) {
            case 0 -> .25f;
            case 1 -> .12f;
            default -> 0f;
        });
        for (Point p : asteroids) {
            tileSelector.getTile(p).setTileType(TileType.ASTEROIDS, this);
        }
    }

    public void init() {
        registerTickable();
    }

    public boolean rendered = false;

    @Override
    public void render(Graphics2D g) {
        levelRenderer.render(g);
        rendered = true;
    }

    @Override
    public void forEachMapStructure(BiConsumer<ObjPos, UnitTeam> action) {
        tileSelector.tileSet.forEach(t -> {
            if (t.hasStructure())
                action.accept(t.renderPos, t.structure.team);
        });
    }

    @Override
    public void forEachMapTileFoW(BiConsumer<ObjPos, Boolean> action) {
        for (Tile t : tileSelector.tileSet) {
            action.accept(t.renderPos, t.isFoW);
        }
    }

    @Override
    public ObjPos getTileBound() {
        return tileBound;
    }

    @Override
    public int tilesX() {
        return tilesX;
    }

    @Override
    public int tilesY() {
        return tilesY;
    }

    @Override
    public void tick(float deltaTime) {
        levelRenderer.tick(deltaTime);
    }

    @Override
    public void delete() {
        removeTickable();
        buttonRegister.delete();
        levelRenderer = null;
        tileSelector.delete();
    }
}
