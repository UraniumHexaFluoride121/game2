package level.editor;

import foundation.input.InputType;
import foundation.math.ObjPos;
import level.LevelEditor;
import level.structure.StructureType;
import level.tile.AbstractTileSelector;
import level.tile.Tile;
import level.tile.TileType;
import unit.type.UnitType;

public class EditorTileSelector extends AbstractTileSelector<LevelEditor> {
    public EditorTileSelector(LevelEditor level) {
        super(level);
    }

    public boolean leftMouseDown = false;

    @Override
    protected void mouseLeft(ObjPos pos, boolean inside, InputType type) {
        super.mouseLeft(pos, inside, type);
        leftMouseDown = true;
        Tile t = tileAtSelectablePos(pos);
        if (t != null) {
            UnitType u = level.levelRenderer.getEditingUnitType();
            if (u != null) {
                if (t.type != TileType.ASTEROIDS || u.moveCost(TileType.ASTEROIDS) < 50)
                    level.unitData[t.pos.x][t.pos.y] = new EditorUnitData(u.getInternalName(), level.levelRenderer.editingTeam.unitTeam, getTile(t.pos).renderPos);
                level.onMapChanged();
            } else if (level.levelRenderer.removeUnit()) {
                level.unitData[t.pos.x][t.pos.y] = new EditorUnitData(null, null, null);
                level.onMapChanged();
            }
            StructureType s = level.levelRenderer.getEditingStructureType();
            if (s != null) {
                if (s == StructureType.BASE) {
                    tileSet.forEach(tile -> {
                        if (tile.hasStructure() && tile.structure.type == StructureType.BASE && tile.structure.team == level.levelRenderer.editingTeam.unitTeam)
                            tile.removeStructure();
                    });
                }
                t.setStructure(s, level.levelRenderer.editingTeam.unitTeam);
                level.onMapChanged();
            } else if (level.levelRenderer.removeStructure()) {
                t.removeStructure();
                level.onMapChanged();
            }
        }
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        super.buttonReleased(pos, inside, blocked, type);
        if (type == InputType.MOUSE_LEFT)
            leftMouseDown = false;
    }
}
