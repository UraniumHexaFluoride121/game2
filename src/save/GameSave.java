package save;

import foundation.math.ObjPos;
import level.GameplaySettings;
import level.Level;
import level.PlayerTeam;
import level.structure.Structure;
import level.structure.StructureType;
import level.tile.Tile;
import level.tile.TileData;
import level.tile.TileType;
import unit.*;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiConsumer;

public class GameSave implements Serializable, LoadedFromSave, TileMapDisplayable {
    @Serial
    private static final long serialVersionUID = 1L;
    public final long seed;
    public final int levelWidth, levelHeight, turn;
    public final HashSet<UnitData> unitData = new HashSet<>();
    public final HashMap<UnitTeam, PlayerTeam> teams, initialTeams;
    public final HashMap<UnitTeam, Float> destroyedUnitsDamage;
    public final HashMap<UnitTeam, Integer> destroyedUnits;
    public final UnitTeam activeTeam;
    public final HashMap<UnitTeam, Integer> availableMap;
    public final HashMap<UnitTeam, Boolean> bots;
    public final HashMap<UnitTeam, Integer> botDestroyedUnitCount = new HashMap<>();
    public final byte[] tiles;
    public final String name;
    public final float botDifficulty;
    public final GameplaySettings gameplaySettings;
    public final ObjPos tileBound;
    private final TileDisplayInfo[][] displayInfo;

    public GameSave(Level level, String name) {
        this.name = name;
        seed = level.seed;
        tileBound = level.tileBound;
        levelWidth = level.tilesX;
        levelHeight = level.tilesY;
        botDifficulty = level.botDifficulty;
        displayInfo = new TileDisplayInfo[levelWidth][];
        try {
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            DataOutputStream w = new DataOutputStream(byteOutput);
            for (int x = 0; x < levelWidth; x++) {
                displayInfo[x] = new TileDisplayInfo[levelHeight];
                for (int y = 0; y < levelHeight; y++) {
                    Tile t = level.getTile(x, y);
                    t.write(w);
                    displayInfo[x][y] = new TileDisplayInfo(t.type, t.renderPos, t.isFoW, t.hasStructure() ? t.structure.type : null, t.hasStructure() ? t.structure.team : null);
                }
            }
            tiles = byteOutput.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        turn = level.getTurn();
        teams = new HashMap<>(level.playerTeam);
        initialTeams = new HashMap<>(level.initialPlayerTeams);
        destroyedUnitsDamage = new HashMap<>(level.destroyedUnitsDamage);
        destroyedUnits = new HashMap<>(level.destroyedUnitsByTeam);
        level.unitSet.forEach(u -> unitData.add(u.data.copy()));
        activeTeam = level.getActiveTeam();
        availableMap = new HashMap<>(level.levelRenderer.energyManager.availableMap);
        bots = level.bots;
        for (UnitTeam team : bots.keySet()) {
            if (bots.get(team))
                botDestroyedUnitCount.put(team, level.botHandlerMap.get(team).getDestroyedUnits());
        }
        gameplaySettings = level.gameplaySettings;
    }

    public void loadLevel(Level level) {
        level.setTurn(activeTeam, turn, false);
        ByteArrayInputStream byteInput = new ByteArrayInputStream(tiles);
        DataInputStream tileReader = new DataInputStream(byteInput);
        TileData[][] data = new TileData[levelWidth][];
        Structure[][] structures = new Structure[levelWidth][];
        try {
            for (int x = 0; x < levelWidth; x++) {
                data[x] = new TileData[levelHeight];
                structures[x] = new Structure[levelHeight];
                for (int y = 0; y < levelHeight; y++) {
                    TileData d = Tile.read(tileReader);
                    data[x][y] = d;
                    if (d.hasStructure()) {
                        structures[x][y] = new Structure(tileReader);
                    } else {
                        structures[x][y] = null;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HashMap<UnitTeam, Point> basePositions = new HashMap<>();
        for (int x = 0; x < levelWidth; x++) {
            for (int y = 0; y < levelHeight; y++) {
                Tile tile = level.getTile(x, y);
                tile.setTileType(data[x][y]);
                Structure structure = structures[x][y];
                if (structure != null) {
                    if (structure.type == StructureType.BASE) {
                        basePositions.put(structure.team, structure.pos);
                    } else {
                        tile.setStructure(structure);
                    }
                } else
                    tile.removeStructure();
            }
        }
        level.setBasePositions(basePositions);
        unitData.forEach(d -> {
            d.copy().getUnit(level, false);
        });
        level.updateFoW();
        level.levelRenderer.energyManager.recalculateIncome();
        level.levelRenderer.energyManager.availableMap = new HashMap<>(availableMap);
        level.levelRenderer.energyManager.updateDisplay(level.getThisTeam());
        level.levelRenderer.pauseMenu.saveFileNameBox.setText(name);
        for (UnitTeam team : bots.keySet()) {
            if (bots.get(team))
                level.botHandlerMap.get(team).loadDestroyedUnits(botDestroyedUnitCount.get(team));
        }
        level.initialPlayerTeams = new HashMap<>(initialTeams);
        level.destroyedUnitsDamage = new HashMap<>(destroyedUnitsDamage);
        level.destroyedUnitsByTeam = new HashMap<>(destroyedUnits);
    }

    @Override
    public void load() {
        unitData.forEach(UnitData::load);
    }

    @Override
    public void forEachUnitMapData(UnitMapDataConsumer action) {
        unitData.forEach(u -> {
            TileDisplayInfo t = displayInfo[u.pos.x][u.pos.y];
            action.accept(t.renderPos, u.team, !u.stealthMode && !t.isFoW);
        });
    }

    @Override
    public ObjPos getTileBound() {
        return tileBound;
    }

    @Override
    public void forEachMapStructure(BiConsumer<ObjPos, UnitTeam> action) {
        for (int x = 0; x < levelWidth; x++) {
            for (int y = 0; y < levelHeight; y++) {
                TileDisplayInfo info = displayInfo[x][y];
                if (info.structureType != null)
                    action.accept(info.renderPos, info.structureTeam);
            }
        }
    }

    @Override
    public void forEachMapTileFoW(BiConsumer<ObjPos, Boolean> action) {
        for (int x = 0; x < levelWidth; x++) {
            for (int y = 0; y < levelHeight; y++) {
                TileDisplayInfo info = displayInfo[x][y];
                action.accept(info.renderPos, info.isFoW);
            }
        }
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
        return displayInfo[x][y].type;
    }

    private record TileDisplayInfo(TileType type, ObjPos renderPos, boolean isFoW, StructureType structureType,
                                   UnitTeam structureTeam) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}
