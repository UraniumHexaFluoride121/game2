package level;

import foundation.tick.TickOrder;
import level.editor.EditorLevelRenderer;
import level.editor.EditorTileSelector;
import level.editor.EditorUnitData;
import level.structure.StructureType;
import level.tile.Tile;
import unit.UnitMapDataConsumer;
import unit.UnitTeam;

import java.util.HashSet;

public class LevelEditor extends AbstractLevel<EditorLevelRenderer, EditorTileSelector> {
    public final int playerCount;
    public final EditorUnitData[][] unitData;
    public boolean unsaved = false, valid = false;

    public LevelEditor(int tilesX, int tilesY, long seed, int playerCount) {
        super(tilesX, tilesY, seed);
        this.playerCount = playerCount;
        levelRenderer = new EditorLevelRenderer(this);
        tileSelector = new EditorTileSelector(this);
        levelRenderer.createRenderers();
        buttonRegister.register(tileSelector);
        tileSelector.tileSet.forEach(t -> t.isFoW = false);

        unitData = new EditorUnitData[tilesX][];
        clearUnits();
        onMapChanged();
    }

    public void clearUnits() {
        for (int x = 0; x < tilesX; x++) {
            unitData[x] = new EditorUnitData[tilesY];
            for (int y = 0; y < tilesY; y++) {
                unitData[x][y] = new EditorUnitData(null, null, null);
            }
        }
    }

    public void onMapChanged() {
        unsaved = true;
        updateMapValidity();
    }

    public void updateMapValidity() {
        valid = false;
        HashSet<UnitTeam> baseTeams = new HashSet<>(), unitTeams = new HashSet<>();
        tileSelector.tileSet.forEach(t -> {
            if (t.hasStructure() && t.structure.type == StructureType.BASE)
                baseTeams.add(t.structure.team);
        });
        if (baseTeams.size() != playerCount) {
            levelRenderer.setInvalid("All players must have a Base structure.");
            return;
        }

        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                EditorUnitData data = unitData[x][y];
                if (data.type() != null)
                    unitTeams.add(data.team());
            }
        }
        if (unitTeams.size() != playerCount) {
            levelRenderer.setInvalid("All players must have at least one unit of any type.");
            return;
        }
        valid = true;
        levelRenderer.setValid();
    }

    @Override
    public TickOrder getTickOrder() {
        return TickOrder.LEVEL;
    }

    @Override
    public void forEachUnitMapData(UnitMapDataConsumer action) {
        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                EditorUnitData data = unitData[x][y];
                if (data.type() != null)
                    action.accept(tiles[x][y].renderPos, data.team(), true);
            }
        }
    }

    @Override
    public void tick(float deltaTime) {
        super.tick(deltaTime);
        if (tileSelector.leftMouseDown && levelRenderer.getEditingTileType() != null) {
            Tile t = tileSelector.mouseOverTile;
            if (t != null && t.type != levelRenderer.getEditingTileType()) {
                t.setTileType(levelRenderer.getEditingTileType(), this);
                onMapChanged();
            }
        }
    }
}