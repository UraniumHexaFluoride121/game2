package render.anim;

import foundation.math.HexagonalDirection;
import foundation.math.ObjPos;
import level.Tile;

import java.awt.*;
import java.util.ArrayList;

public class AnimTilePath {
    private final ArrayList<Point> tiles = new ArrayList<>();
    public final ArrayList<HexagonalDirection> directions = new ArrayList<>();
    private final ArrayList<ObjPos> points = new ArrayList<>();
    private AnimationTimer timer;

    public AnimTilePath(ArrayList<Point> tiles) {
        tiles.forEach(this::addTile);
    }

    public AnimTilePath() {
    }

    public void setTimer(AnimationTimer timer) {
        this.timer = timer;
    }

    public int length() {
        return tiles.size();
    }

    public Point getLastTile() {
        return tiles.getLast();
    }

    public boolean finished() {
        return timer.finished();
    }

    public void addTile(Point p) {
        if (!tiles.isEmpty()) {
            directions.add(HexagonalDirection.directionTo(tiles.getLast(), p));
        }
        tiles.add(p);
        points.add(Tile.getRenderPos(p));
    }

    public ObjPos getEnd() {
        return points.getLast();
    }

    public ObjPos getPos() {
        float t = timer.normalisedProgress();
        if (t == 1)
            return points.getLast();
        int segments = tiles.size() - 1;
        if (segments == -1)
            return null;
        if (segments == 0 || t == 0)
            return points.getFirst();
        int segment = (int) (t * segments);
        return points.get(segment).lerp(points.get(segment + 1), t * segments - segment);
    }

    public HexagonalDirection getDirection() {
        float t = timer.normalisedProgress();
        if (t == 1)
            return directions.getLast();
        int segments = tiles.size() - 1;
        if (segments == -1 || segments == 0)
            return null;
        if (t == 0)
            return directions.getFirst();
        int segment = (int) (t * segments);
        return directions.get(segment);
    }
}
