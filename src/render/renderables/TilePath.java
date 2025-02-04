package render.renderables;

import foundation.MainPanel;
import foundation.math.HexagonalDirection;
import foundation.math.ObjPos;
import level.Level;
import level.Tile;
import level.TileSelector;
import render.Renderable;
import render.anim.AnimTilePath;
import render.anim.PowAnimation;
import unit.UnitType;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class TilePath implements Renderable {
    public static final BasicStroke LINE_STROKE = new BasicStroke(0.5f * SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final BasicStroke LINE_STROKE_INNER = new BasicStroke(0.2f * SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final Color MOVE_PATH_COLOUR_INNER = Tile.BLUE_HIGHLIGHT_COLOUR;
    public static final Color MOVE_PATH_COLOUR = new Color(61, 148, 193);
    public static final float END_DOT_RADIUS = 0.15f, END_DOT_RADIUS_INNER = 0.12f;
    private final UnitType type;
    private final HashSet<Point> tiles;
    private final Point origin;
    private Point end = null;
    private final TileSelector tileSelector;

    private ArrayList<Point> path = new ArrayList<>();

    public TilePath(UnitType type, HashSet<Point> tiles, Point origin, TileSelector tileSelector) {
        this.type = type;
        this.tiles = tiles;
        this.origin = origin;
        this.tileSelector = tileSelector;
    }

    public void setEnd(Point end, Level level) {
        if (end.equals(this.end))
            return;
        if (!tiles.contains(end) || end.equals(origin)) {
            this.end = null;
            path.clear();
            return;
        }
        if (MainPanel.controlHeld) {
            if (path.contains(end)) {
                int index = path.indexOf(end);
                path = new ArrayList<>(path.subList(0, index + 1));
            } else if (HexagonalDirection.isNextTo(end, this.end)) {
                path.add(end);
                AtomicReference<Float> cost = new AtomicReference<>(0f);
                path.forEach(p -> cost.updateAndGet(v -> v + type.tileMovementCostFunction.apply(tileSelector.getTile(p).type)));
                if (cost.get() > type.maxMovement) {
                    setShortestPath(end, level);
                }
            } else {
                setShortestPath(end, level);
            }
        } else {
            setShortestPath(end, level);
        }
        this.end = end;
    }

    private void setShortestPath(Point end, Level level) {
        path = level.tileSelector.shortestPathTo(origin, end, tiles, type.tileMovementCostFunction);
    }

    public Point getLastTile() {
        return path.getLast();
    }

    public int length() {
        return path.size();
    }

    public Point getTile(int index) {
        return path.get(index);
    }

    @Override
    public void render(Graphics2D g) {
        if (end == null)
            return;
        g.translate(0, Tile.TILE_SIZE / 2 * Tile.SIN_60_DEG);
        g.scale(1d / SCALING, 1d / SCALING);
        renderPath(LINE_STROKE, MOVE_PATH_COLOUR, END_DOT_RADIUS, g);
        renderPath(LINE_STROKE_INNER, MOVE_PATH_COLOUR_INNER, END_DOT_RADIUS_INNER, g);
        g.scale(SCALING, SCALING);
        g.translate(0, -Tile.TILE_SIZE / 2 * Tile.SIN_60_DEG);
    }

    private void renderPath(Stroke stroke, Color colour, float dotRadius, Graphics2D g) {
        g.setColor(colour);
        g.setStroke(stroke);
        for (int i = 0; i < path.size(); i++) {
            ObjPos p1 = Tile.getRenderPos(i == 0 ? origin : path.get(i - 1));
            ObjPos p2 = Tile.getRenderPos(path.get(i));
            g.drawLine(
                    (int) (p1.x * SCALING),
                    (int) (p1.y * SCALING),
                    (int) (p2.x * SCALING),
                    (int) (p2.y * SCALING)
            );
            if (i == path.size() - 1)
                g.fillOval(
                        (int) ((p2.x - Tile.TILE_SIZE * dotRadius) * SCALING),
                        (int) ((p2.y - Tile.TILE_SIZE * dotRadius) * SCALING),
                        (int) (Tile.TILE_SIZE * dotRadius * 2 * SCALING),
                        (int) (Tile.TILE_SIZE * dotRadius * 2 * SCALING)
                );
        }
    }

    public AnimTilePath getAnimPath(Predicate<Point> illegalTile) {
        AnimTilePath anim = new AnimTilePath();
        anim.addTile(origin);
        for (Point t : path) {
            if (illegalTile.test(t))
                break;
            anim.addTile(t);
        }
        anim.setTimer(new PowAnimation(anim.length() * 0.3f, 0.8f));
        return anim;
    }
}
