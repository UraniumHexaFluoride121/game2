package render.level.tile;

import foundation.math.HexagonCorner;
import foundation.math.HexagonalDirection;
import foundation.math.ObjPos;
import level.tile.Tile;
import level.tile.TileSet;
import render.Renderable;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.HashSet;

public class HexagonBorder implements Renderable {
    private static final BasicStroke stroke = new BasicStroke(Tile.HIGHLIGHT_STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    private final Path2D.Float path;
    private Color colour;

    public HexagonBorder(TileSet tiles, Color colour) {
        this.colour = colour;
        path = new Path2D.Float();
        HashMap<Point, HashSet<HexagonalDirection>> borders = new HashMap<>(), usedBorders = new HashMap<>();
        for (Point tile : tiles) {
            for (HexagonalDirection d : HexagonalDirection.values()) {
                if (!tiles.contains(d.offset(tile))) {
                    borders.putIfAbsent(tile, new HashSet<>());
                    borders.get(tile).add(d);
                }
            }
        }
        while (!borders.isEmpty()) {
            Point start = borders.keySet().iterator().next(), current = start;
            HashSet<HexagonalDirection> tileBorders = borders.get(start);
            HexagonalDirection startDirection = tileBorders.iterator().next();
            ObjPos startPos = Tile.getCornerRenderPos(start, startDirection.counterClockwiseCorner());
            path.moveTo(startPos.x, startPos.y);
            HexagonalDirection d = startDirection;
            while (true) {
                ObjPos pos = Tile.getCornerRenderPos(current, d.clockwiseCorner());
                path.lineTo(pos.x, pos.y);

                usedBorders.putIfAbsent(current, new HashSet<>());
                usedBorders.get(current).add(d);

                HexagonCorner.BorderInfo nextBorder = d.clockwiseCorner().getConnectingCorner(current, borders);
                if (nextBorder != null) {
                    d = nextBorder.border();
                    current = nextBorder.point();
                } else
                    d = d.clockwise();
                usedBorders.putIfAbsent(current, new HashSet<>());
                usedBorders.get(current).add(d);
                if (d.clockwiseCorner() == startDirection.counterClockwiseCorner() && current.equals(start))
                    break;
                if (d.counterClockwiseCorner() == startDirection.counterClockwiseCorner() && current.equals(start))
                    break;
            }
            usedBorders.forEach((p, set) -> {
                borders.get(p).removeAll(set);
                if (borders.get(p).isEmpty())
                    borders.remove(p);
            });
            usedBorders.clear();
            path.closePath();
        }
    }

    public HexagonBorder setColor(Color colour) {
        this.colour = colour;
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        g.setStroke(stroke);
        g.setColor(colour);
        g.draw(path);
    }
}
