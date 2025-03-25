package render.anim;

import foundation.math.HexagonalDirection;
import foundation.math.ObjPos;
import level.Level;
import level.tile.Tile;
import network.PacketReceiver;
import network.PacketWriter;
import network.Writable;
import render.level.tile.TilePath;
import unit.Unit;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AnimTilePath implements Writable {
    private final ArrayList<Point> tiles = new ArrayList<>();
    public final ArrayList<HexagonalDirection> directions = new ArrayList<>();
    private final ArrayList<ObjPos> points = new ArrayList<>();
    private AnimationTimer timer;

    public AnimTilePath(ArrayList<Point> tiles) {
        tiles.forEach(this::addTile);
    }

    public AnimTilePath(DataInputStream reader) throws IOException {
        ArrayList<Point> tiles = PacketReceiver.readCollection(new ArrayList<>(), () -> PacketReceiver.readPoint(reader), reader);
        tiles.forEach(this::addTile);
        timer = AnimationType.readTimer(reader);
        timer.startTimer();
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

    public void startTimer() {
        timer.startTimer();
    }

    public AnimationTimer getTimer() {
        return timer;
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

    public int getEnergyCost(Unit unit, Level level) {
        ArrayList<Point> path = new ArrayList<>(tiles);
        path.removeFirst();
        return TilePath.getEnergyCost(unit.type, path, level);
    }

    public Point illegalTile(TilePath original) {
        Point illegalTile;
        if (length() != original.length() + 1) {
            illegalTile = original.getTile(length() - 1);
        } else
            illegalTile = null;
        return illegalTile;
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        PacketWriter.writeCollection(tiles, p -> PacketWriter.writePoint(p, w), w);
        timer.write(w);
    }
}
