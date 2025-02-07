package render.renderables;

import foundation.math.ObjPos;
import level.tile.Tile;
import render.Renderable;

import java.awt.*;
import java.awt.geom.Path2D;

public class HexagonRenderer implements Renderable {
    private final Path2D.Float path = new Path2D.Float();
    private final Stroke stroke;
    private final boolean fill;
    private Color color;

    public HexagonRenderer(float size, boolean fill, float strokeWidth, Color color) {
        this.color = color;
        this.fill = fill;
        stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
        path.moveTo(-size / 4, 0);
        path.lineTo(-size / 2, size / 2 * Tile.SIN_60_DEG);
        path.lineTo(-size / 4, size * Tile.SIN_60_DEG);
        path.lineTo(size / 4, size * Tile.SIN_60_DEG);
        path.lineTo(size / 2, size / 2 * Tile.SIN_60_DEG);
        path.lineTo(size / 4, 0);
        path.closePath();
    }

    public HexagonRenderer(float size, float inset, boolean fill, float strokeWidth, Color color) {
        this.color = color;
        this.fill = fill;
        inset = Math.min(inset, size / 2);
        stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
        path.moveTo(-(size - inset * 2) / 4, inset * Tile.SIN_60_DEG);
        path.lineTo(-(size - inset * 2) / 2, size / 2 * Tile.SIN_60_DEG);
        path.lineTo(-(size - inset * 2) / 4, (size - inset) * Tile.SIN_60_DEG);
        path.lineTo((size - inset * 2) / 4, (size - inset) * Tile.SIN_60_DEG);
        path.lineTo((size - inset * 2) / 2, size / 2 * Tile.SIN_60_DEG);
        path.lineTo((size - inset * 2) / 4, inset * Tile.SIN_60_DEG);
        path.closePath();
    }

    public boolean pointInside(ObjPos pos) {
        return path.contains(pos.x, pos.y);
    }

    public HexagonRenderer setColor(Color color) {
        this.color = color;
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(color);
        if (fill) {
            g.fill(path);
        } else {
            g.setStroke(stroke);
            g.draw(path);
        }
    }
}
