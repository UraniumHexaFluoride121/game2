package level.tutorial.sequence;

import foundation.math.ObjPos;

public enum BoxSize {
    SMALL(new ObjPos(7, 3)),
    SMALL_MEDIUM(new ObjPos(10, 3.5f)),
    MEDIUM(new ObjPos(12, 4.25f)),
    MEDIUM_TALL(new ObjPos(12, 6.5f)),
    LARGE(new ObjPos(15, 8.5f)),
    EXTRA_LARGE(new ObjPos(17, 10)),
    EXTRA_EXTRA_LARGE(new ObjPos(19, 12));

    public final ObjPos size;

    BoxSize(ObjPos size) {
        this.size = size;
    }
}
