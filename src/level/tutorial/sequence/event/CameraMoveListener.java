package level.tutorial.sequence.event;

import foundation.math.ObjPos;

public class CameraMoveListener implements TutorialEventListener {
    private final float distance;
    private float totalCameraMove = 0;
    private ObjPos prev = null;

    private CameraMoveListener(float distance) {
        this.distance = distance;
    }

    public static CameraMoveListener distance(float distance) {
        return new CameraMoveListener(distance);
    }

    @Override
    public boolean test(TutorialEvent e) {
        if (e instanceof EventCameraPos c) {
            if (c.preMove) {
                prev = c.pos.copy();
            } else {
                totalCameraMove += c.pos.distance(prev);
            }
            return totalCameraMove >= distance;
        }
        return false;
    }
}
