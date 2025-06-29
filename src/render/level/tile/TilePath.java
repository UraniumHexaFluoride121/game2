package render.level.tile;

import foundation.MainPanel;
import foundation.math.HexagonalDirection;
import foundation.math.ObjPos;
import level.Level;
import level.energy.EnergyCostDisplay;
import level.energy.EnergyDisplay;
import level.energy.EnergyManager;
import level.tile.Tile;
import level.tile.TileSelector;
import level.tile.TileSet;
import level.tile.TileType;
import render.GameRenderer;
import render.Renderable;
import render.anim.AnimTilePath;
import render.anim.LerpAnimation;
import render.anim.PowAnimation;
import render.types.text.StyleElement;
import unit.stats.StatManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

public class TilePath implements Renderable {
    public static final BasicStroke LINE_STROKE = new BasicStroke(0.5f * SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final BasicStroke LINE_STROKE_INNER = new BasicStroke(0.2f * SCALING, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    public static final Color MOVE_PATH_COLOUR_INNER = Tile.BLUE_HIGHLIGHT_COLOUR;
    public static final Color MOVE_PATH_COLOUR = new Color(61, 148, 193);
    public static final Color MOVE_PATH_INVALID_COLOUR_INNER = Tile.ILLEGAL_TILE_COLOUR;
    public static final Color MOVE_PATH_INVALID_COLOUR = new Color(193, 61, 61);
    public static final float END_DOT_RADIUS = 0.15f, END_DOT_RADIUS_INNER = 0.12f;
    private static final EnergyCostDisplay energyCostDisplay = new EnergyCostDisplay(false);
    private static final EnergyDisplay invalidRangeDisplay = new EnergyDisplay(20, true).setText(StyleElement.ENERGY_COST_RED.display + "Move path too long");
    private static final EnergyDisplay invalidCostDisplay = new EnergyDisplay(20, true).setText(StyleElement.ENERGY_COST_RED.display + "Insufficient " + EnergyManager.displayName);
    private final TileSet tiles;
    private final Point origin;
    private Point end = null;
    private final TileSelector tileSelector;
    private final LerpAnimation anim = new LerpAnimation(1);

    private boolean rangeValid = false, costValid = false;

    private ArrayList<Point> path = new ArrayList<>();

    public TilePath(TileSet tiles, Point origin, TileSelector tileSelector) {
        this.tiles = tiles;
        this.origin = origin;
        this.tileSelector = tileSelector;
    }

    public void setEnd(Point end, StatManager stats, Level level) {
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
            } else {
                setShortestPath(end, stats, level);
            }
        } else {
            setShortestPath(end, stats, level);
        }
        this.end = end;
        validate(stats);
    }

    public void setShortestPath(Point end, StatManager stats, Level level) {
        path = level.tileSelector.shortestPathTo(origin, end, tiles, stats::moveCost);
        validate(stats);
    }

    private void validate(StatManager stats) {
        AtomicReference<Float> cost = new AtomicReference<>(0f);
        path.forEach(p -> cost.updateAndGet(v -> v + stats.moveCost(tileSelector.getTile(p).type)));
        rangeValid = cost.get() <= stats.maxMovement();
        costValid = stats.canAffordMove(this);
    }

    public boolean isRangeValid() {
        return rangeValid;
    }

    public static int getEnergyCost(ArrayList<Point> path, StatManager stats, Level level) {
        float cost = getPathCost(path, stats::moveCost, level);
        return stats.moveEnergyCost(cost);
    }

    public float getPathCost(Function<TileType, Float> perTileCost, Level level) {
        return getPathCost(path, perTileCost, level);
    }

    private static float getPathCost(ArrayList<Point> path, Function<TileType, Float> perTileCost, Level level) {
        float cost = 0;
        for (Point t : path) {
            cost += perTileCost.apply(level.getTile(t).type);
        }
        return cost;
    }

    public static int getEnergyCost(StatManager stats, HashMap<Point, Float> costMap, Point to) {
        return stats.moveEnergyCost(costMap.get(to));
    }

    public int getEnergyCost(StatManager stats, Level level) {
        return getEnergyCost(path, stats, level);
    }

    public Point getLastTile() {
        return path.isEmpty() ? null : path.getLast();
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
        boolean valid = costValid && rangeValid;
        g.translate(0, Tile.TILE_SIZE / 2 * Tile.SIN_60_DEG);
        g.scale(1d / SCALING, 1d / SCALING);
        renderPath(LINE_STROKE, valid ? MOVE_PATH_COLOUR : MOVE_PATH_INVALID_COLOUR, END_DOT_RADIUS, g);
        renderPath(LINE_STROKE_INNER, valid ? MOVE_PATH_COLOUR_INNER : MOVE_PATH_INVALID_COLOUR_INNER, END_DOT_RADIUS_INNER, g);
        g.scale(SCALING, SCALING);
        g.translate(0, -Tile.TILE_SIZE / 2 * Tile.SIN_60_DEG);
    }

    public void renderEnergyCost(Graphics2D g, StatManager stats, Level level) {
        if (end == null)
            return;
        energyCostDisplay.setCost(-getEnergyCost(stats, level), level);
        GameRenderer.renderOffset(level.getTile(end).renderPos, g, () -> {
            g.translate(0, -1.5f);
            if (!rangeValid) {
                invalidRangeDisplay.render(g);
                g.translate(0, -1.2f);
            } else if (!costValid) {
                invalidCostDisplay.render(g);
                g.translate(0, -1.2f);
            }
            energyCostDisplay.render(g);
            energyCostDisplay.renderToEnergyManager(level);
        });
    }

    private static final float LINE_LENGTH = 0.6f;

    private void renderPath(Stroke stroke, Color colour, float dotRadius, Graphics2D g) {
        if (anim.finished())
            anim.loopTimer();
        g.setColor(colour);
        g.setStroke(stroke);
        for (int i = 0; i < path.size(); i++) {
            ObjPos start = Tile.getRenderPos(i == 0 ? origin : path.get(i - 1));
            ObjPos end = Tile.getRenderPos(path.get(i));
            float t = anim.normalisedProgress();
            ObjPos p1 = start.lerp(end, t);
            ObjPos p2 = start.lerp(end, Math.clamp(t + LINE_LENGTH, 0, 1));
            g.drawLine(
                    (int) (p1.x * SCALING),
                    (int) (p1.y * SCALING),
                    (int) (p2.x * SCALING),
                    (int) (p2.y * SCALING)
            );
            if (t > 1 - LINE_LENGTH) {
                ObjPos p3 = start.lerp(end, t - (1 - LINE_LENGTH));
                g.drawLine(
                        (int) (start.x * SCALING),
                        (int) (start.y * SCALING),
                        (int) (p3.x * SCALING),
                        (int) (p3.y * SCALING)
                );
            }
            if (i == path.size() - 1)
                g.fillOval(
                        (int) ((end.x - Tile.TILE_SIZE * dotRadius) * SCALING),
                        (int) ((end.y - Tile.TILE_SIZE * dotRadius) * SCALING),
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
        anim.setTimer(new PowAnimation(anim.length() * 0.25f, 0.8f));
        return anim;
    }
}
