package level;

import foundation.Deletable;
import foundation.input.ButtonRegister;
import foundation.math.ObjPos;
import foundation.math.RandomHandler;
import foundation.tick.RegisteredTickable;
import level.tile.*;
import render.Renderable;
import unit.TileMapDisplayable;
import unit.UnitTeam;

import java.awt.*;
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

    @Override
    public TileType getTileType(int x, int y) {
        return tileSelector.getTile(x, y).type;
    }

    public void createRandom(long seed) {
        this.seed = seed;
        random = new RandomHandler(seed);
    }

    public void generateTiles() {
        tileSelector.tileSet.forEach(t -> t.setTileType(TileType.EMPTY, this));
        TileSet nebula = TileSet.all(this).m(this, t -> t
                .pointTerrain(20, 3, r -> switch (r) {
                    case 0 -> .55f;
                    case 1 -> .25f;
                    case 2 -> .1f;
                    default -> 0f;
                }));
        for (Point p : nebula) {
            tileSelector.getTile(p).setTileType(TileType.NEBULA, this);
        }
        TileSet denseNebula = TileSet.all(this).m(this, t -> t
                .tileFilter(TileModifier.tileOfType(TileType.NEBULA))
                .pointTerrain(10, 1, r -> switch (r) {
                    case 0 -> .1f;
                    default -> 0f;
                }));
        for (Point p : denseNebula) {
            tileSelector.getTile(p).setTileType(TileType.DENSE_NEBULA, this);
        }
        TileSet asteroids = TileSet.all(this).m(this, t -> t
                .tileFilter(TileModifier.tileOfType(TileType.EMPTY))
                .pointTerrain(15, 2, r -> switch (r) {
            case 0 -> .18f;
            case 1 -> .1f;
            default -> 0f;
        }));
        for (Point p : asteroids) {
            tileSelector.getTile(p).setTileType(TileType.ASTEROIDS, this);
        }
    }

    public void init() {
        registerTickable();
    }

    public Tile getTile(Point pos) {
        return tiles[pos.x][pos.y];
    }

    public Tile getTile(int x, int y) {
        return tiles[x][y];
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
