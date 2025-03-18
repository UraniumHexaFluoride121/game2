package save;

import level.Level;
import level.PlayerTeam;
import level.structure.Structure;
import level.structure.StructureType;
import level.tile.Tile;
import level.tile.TileData;
import unit.UnitData;
import unit.UnitTeam;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;

public class GameSave implements Serializable, LoadedFromSave {
    @Serial
    private static final long serialVersionUID = 1L;
    public final long seed;
    public final int levelWidth, levelHeight, turn;
    public final HashSet<UnitData> unitData = new HashSet<>();
    public final HashMap<UnitTeam, PlayerTeam> teams;
    public final UnitTeam activeTeam;
    public final HashMap<UnitTeam, Integer> availableMap;
    public final HashMap<UnitTeam, Boolean> bots;
    public final HashMap<UnitTeam, Integer> botDestroyedUnitCount = new HashMap<>();
    public final byte[] tiles;
    public final String name;
    public final float botDifficulty;

    public GameSave(Level level, String name) {
        this.name = name;
        seed = level.seed;
        levelWidth = level.tilesX;
        levelHeight = level.tilesY;
        botDifficulty = level.botDifficulty;
        try {
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            DataOutputStream w = new DataOutputStream(byteOutput);
            for (int x = 0; x < levelWidth; x++) {
                for (int y = 0; y < levelHeight; y++) {
                    level.getTile(x, y).write(w);
                }
            }
            tiles = byteOutput.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        turn = level.getTurn();
        teams = new HashMap<>(level.playerTeam);
        level.unitSet.forEach(u -> unitData.add(new UnitData(u)));
        activeTeam = level.getActiveTeam();
        availableMap = new HashMap<>(level.levelRenderer.energyManager.availableMap);
        bots = level.bots;
        for (UnitTeam team : bots.keySet()) {
            if (bots.get(team))
                botDestroyedUnitCount.put(team, level.botHandlerMap.get(team).getDestroyedUnits());
        }
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
            d.getUnit(level, false);
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
    }

    @Override
    public void load() {
        unitData.forEach(UnitData::load);
    }
}
