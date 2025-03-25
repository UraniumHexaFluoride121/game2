package level.editor;

import foundation.math.ObjPos;
import level.structure.StructureType;
import level.tile.TileType;
import unit.UnitTeam;

import java.io.Serial;
import java.io.Serializable;

public record EditorTileData(TileType type, ObjPos renderPos, double randomValue, StructureType structureType,
                             UnitTeam structureTeam) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
