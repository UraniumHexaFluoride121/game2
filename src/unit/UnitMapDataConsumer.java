package unit;

import foundation.math.ObjPos;

public interface UnitMapDataConsumer {
    void accept(ObjPos renderPos, UnitTeam team, boolean renderVisible);
}
