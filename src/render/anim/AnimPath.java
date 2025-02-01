package render.anim;

import foundation.math.ObjPos;

import java.util.ArrayList;

public class AnimPath {
    private ArrayList<ObjPos> points = new ArrayList<>();
    private final AnimationTimer timer;

    public AnimPath(ArrayList<ObjPos> points, AnimationTimer timer) {
        this.points = points;
        this.timer = timer;
    }

    public AnimPath(AnimationTimer timer) {
        this.timer = timer;
    }

    public boolean finished() {
        return timer.finished();
    }

    public void addPoint(ObjPos p) {
        points.add(p);
    }

    public ObjPos getEnd() {
        return points.getLast();
    }

    public ObjPos getPos() {
        float t = timer.normalisedProgress();
        if (t == 1)
            return points.getLast();
        int segments = points.size() - 1;
        if (segments == -1)
            return null;
        if (segments == 0 || t == 0)
            return points.getFirst();
        int segment = (int) (t * segments);
        return points.get(segment).lerp(points.get(segment + 1), t * segments - segment);
    }
}
