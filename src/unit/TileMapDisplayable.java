package unit;

import foundation.math.ObjPos;

import java.util.function.BiConsumer;

public interface TileMapDisplayable {
    void forEachUnitMapData(UnitMapDataConsumer action);
    ObjPos getTileBound();
    void forEachMapStructure(BiConsumer<ObjPos, UnitTeam> action);
    void forEachMapTileFoW(BiConsumer<ObjPos, Boolean> action);
    int tilesX();
    int tilesY();
}
