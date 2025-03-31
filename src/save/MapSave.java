package save;

import foundation.math.ObjPos;
import level.Level;
import level.LevelEditor;
import level.editor.EditorTileData;
import level.editor.EditorUnitData;
import level.structure.StructureType;
import level.tile.Tile;
import level.tile.TileType;
import level.tutorial.TutorialManager;
import unit.TileMapDisplayable;
import unit.Unit;
import unit.UnitMapDataConsumer;
import unit.UnitTeam;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class MapSave implements Serializable, LoadedFromSave, TileMapDisplayable {
    @Serial
    private static final long serialVersionUID = 1L;
    public final long seed;
    public final int levelWidth, levelHeight, playerCount;
    public final EditorTileData[][] tileData;
    public final EditorUnitData[][] unitData;
    public final boolean valid;
    private final ObjPos tileBound;
    private final String name;

    public MapSave(LevelEditor level, String name) {
        this.name = name;
        seed = level.seed;
        levelWidth = level.tilesX;
        levelHeight = level.tilesY;
        playerCount = level.playerCount;
        valid = level.valid;
        tileBound = level.tileBound.copy();
        tileData = new EditorTileData[levelWidth][];
        unitData = new EditorUnitData[levelWidth][];
        for (int x = 0; x < levelWidth; x++) {
            tileData[x] = new EditorTileData[levelHeight];
            unitData[x] = new EditorUnitData[levelHeight];
            for (int y = 0; y < levelHeight; y++) {
                Tile t = level.tileSelector.getTile(x, y);
                tileData[x][y] = new EditorTileData(t.type, t.renderPos, t.randomValue, t.hasStructure() ? t.structure.type : null, t.hasStructure() ? t.structure.team : null);
                unitData[x][y] = level.unitData[x][y];
            }
        }
    }

    public LevelEditor createLevelEditor() {
        LevelEditor level = new LevelEditor(levelWidth, levelHeight, seed, playerCount);
        for (int x = 0; x < levelWidth; x++) {
            for (int y = 0; y < levelHeight; y++) {
                Tile t = level.tileSelector.getTile(x, y);
                EditorTileData data = tileData[x][y];
                t.setTileType(data.type(), data.randomValue());
                if (data.structureType() != null)
                    t.setStructure(data.structureType(), data.structureTeam());
                EditorUnitData u = unitData[x][y];
                u.load();
                level.unitData[x][y] = u;
            }
        }
        level.onMapChanged();
        level.unsaved = false;
        level.levelRenderer.setSaveName(name);
        return level;
    }

    public void loadLevel(Level level) {
        HashMap<UnitTeam, Point> basePositions = new HashMap<>();
        for (int x = 0; x < levelWidth; x++) {
            for (int y = 0; y < levelHeight; y++) {
                Tile t = level.tileSelector.getTile(x, y);
                EditorTileData data = tileData[x][y];
                t.setTileType(data.type(), data.randomValue());
                if (data.structureType() != null) {
                    t.setStructure(data.structureType(), data.structureTeam());
                    if (data.structureType() == StructureType.BASE)
                        basePositions.put(data.structureTeam(), new Point(x, y));
                }
                EditorUnitData u = unitData[x][y];
                u.load();
                if (u.type() != null)
                    level.addUnit(new Unit(u.unitType, u.team(), new Point(x, y), level));
            }
        }
        level.setBasePositions(basePositions);
        level.levelRenderer.energyManager.recalculateIncome();
        level.levelRenderer.energyManager.incrementTurn(level.getThisTeam());
        if (TutorialManager.isTutorial())
            TutorialManager.createSequence(level);
    }

    @Override
    public void load() {

    }

    @Override
    public void forEachUnitMapData(UnitMapDataConsumer action) {
        for (int x = 0; x < levelWidth; x++) {
            for (int y = 0; y < levelHeight; y++) {
                EditorUnitData u = unitData[x][y];
                if (u.type() != null)
                    action.accept(u.renderPos, u.team(), true);
            }
        }
    }

    @Override
    public ObjPos getTileBound() {
        return tileBound;
    }

    @Override
    public void forEachMapStructure(BiConsumer<ObjPos, UnitTeam> action) {
        for (int x = 0; x < levelWidth; x++) {
            for (int y = 0; y < levelHeight; y++) {
                EditorTileData t = tileData[x][y];
                if (t.structureType() != null)
                    action.accept(t.renderPos(), t.structureTeam());
            }
        }
    }

    @Override
    public void forEachMapTileFoW(BiConsumer<ObjPos, Boolean> action) {

    }

    @Override
    public int tilesX() {
        return levelWidth;
    }

    @Override
    public int tilesY() {
        return levelHeight;
    }

    @Override
    public TileType getTileType(int x, int y) {
        return tileData[x][y].type();
    }
}
