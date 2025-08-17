package render.level.tile;

import foundation.MainPanel;
import foundation.math.HexagonalDirection;
import foundation.math.ObjPos;
import level.Level;
import level.tile.Tile;
import level.tile.TileSet;
import render.GameRenderer;
import unit.stats.UnitStatManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class TilePath extends AbstractTilePath {
    private final TileSet tiles;

    public TilePath(TileSet tiles, Point origin) {
        super(origin);
        this.tiles = tiles;
    }

    public void setEnd(Point end, UnitStatManager stats, Level level) {
        if (end.equals(this.end))
            return;
        if (!tiles.contains(end) || end.equals(origin)) {
            this.end = null;
            path.clear();
            validate(stats);
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

    public void setShortestPath(Point end, UnitStatManager stats, Level level) {
        path = level.tileSelector.shortestPathTo(origin, end, tiles, stats::moveCost);
        validate(stats);
    }

    private void validate(UnitStatManager stats) {
        AtomicReference<Float> cost = new AtomicReference<>(0f);
        path.forEach(p -> cost.updateAndGet(v -> v + stats.moveCost(stats.u.getLevel().tileSelector.getTile(p).type)));
        rangeValid = cost.get() <= stats.maxMovement();
        costValid = stats.canAffordMove(this);
    }

    public void renderEnergyCost(Graphics2D g, UnitStatManager stats, Level level) {
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

    @Override
    protected ObjPos getRenderPos(Point tile) {
        return Tile.getRenderPos(tile);
    }
}
