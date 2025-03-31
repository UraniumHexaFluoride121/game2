package level.tutorial.sequence;

import foundation.math.ObjPos;

public enum BoxSize {
    SMALL(new ObjPos(7, 3)),
    MEDIUM(new ObjPos(12, 4.25f)),
    MEDIUM_TALL(new ObjPos(12, 6.5f)),
    LARGE(new ObjPos(15, 8.5f));

    public final ObjPos size;

    BoxSize(ObjPos size) {
        this.size = size;
    }
}
