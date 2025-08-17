package level.tutorial.sequence.event;

import foundation.math.ObjPos;

public class EventCameraPos extends TutorialEvent {
    public final boolean preMove;
    public final ObjPos pos;

    public EventCameraPos(boolean preMove, ObjPos pos) {
        super(null);
        this.preMove = preMove;
        this.pos = pos;
    }
}
