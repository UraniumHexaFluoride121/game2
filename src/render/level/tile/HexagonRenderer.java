package render.level.tile;

import foundation.math.MathUtil;
import foundation.math.ObjPos;
import level.tile.Tile;
import render.Renderable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

public class HexagonRenderer implements Renderable {
    private final Path2D.Float path;
    private final Stroke stroke;
    private final boolean fill;
    private Color color;

    public HexagonRenderer(float size, boolean fill, float strokeWidth, Color color) {
        this(size, 0, fill, strokeWidth, color);
    }

    public HexagonRenderer(float size, float inset, boolean fill, float strokeWidth, Color color) {
        this.color = color;
        this.fill = fill;
        inset = Math.min(inset, size / 2);
        stroke = Renderable.roundedStroke(strokeWidth);
        path = hexagonShape(size, inset, false);
    }

    public HexagonRenderer(boolean fill, float strokeWidth, Color color) {
        this.color = color;
        this.fill = fill;
        stroke = Renderable.roundedStroke(strokeWidth);
        path = new Path2D.Float();
    }

    public HexagonRenderer addSegment(Path2D.Float segment) {
        path.append(segment, false);
        return this;
    }

    public HexagonRenderer rotate(float size) {
        AffineTransform t = new AffineTransform();
        t.rotate(Math.toRadians(30), 0, size / 2 * Tile.SIN_60_DEG);
        path.transform(t);
        return this;
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

    public static Path2D.Float hexagonShape(float size, float inset, boolean centered) {
        Path2D.Float path = new Path2D.Float();
        for (int i = 0; i < 6; i++) {
            ObjPos point = hexagonPoint(size, inset, centered, i);
            if (i == 0)
                path.moveTo(point.x, point.y);
            else
                path.lineTo(point.x, point.y);
        }
        path.closePath();
        return path;
    }

    public static ObjPos hexagonPoint(float size, float inset, boolean centered, int index) {
        float yOffset = centered ? -size / 2 * Tile.SIN_60_DEG : 0;
        return switch (index % 6) {
            case 0 -> new ObjPos(-(size - inset * 2) / 4, inset * Tile.SIN_60_DEG + yOffset);
            case 1 -> new ObjPos(-(size - inset * 2) / 2, size / 2 * Tile.SIN_60_DEG + yOffset);
            case 2 -> new ObjPos(-(size - inset * 2) / 4, (size - inset) * Tile.SIN_60_DEG + yOffset);
            case 3 -> new ObjPos((size - inset * 2) / 4, (size - inset) * Tile.SIN_60_DEG + yOffset);
            case 4 -> new ObjPos((size - inset * 2) / 2, size / 2 * Tile.SIN_60_DEG + yOffset);
            case 5 -> new ObjPos((size - inset * 2) / 4, inset * Tile.SIN_60_DEG + yOffset);
            default -> throw new RuntimeException();
        };
    }

    public static Path2D.Float hexagonSegment(float size, float inset, boolean centered, float start, float end) {
        start = (start % 6 + 6) % 6;
        end = (end % 6 + 6) % 6;
        ObjPos startPoint = hexagonPoint(size, inset, centered, (int) start).lerp(hexagonPoint(size, inset, centered, (int) start + 1), MathUtil.fraction(start));
        ObjPos endPoint = hexagonPoint(size, inset, centered, (int) end).lerp(hexagonPoint(size, inset, centered, (int) end + 1), MathUtil.fraction(end));
        int endIndex = start > end ? (int) end + 6 : (int) end;
        Path2D.Float path = new Path2D.Float();
        path.moveTo(startPoint.x, startPoint.y);
        for (int i = (int) start + 1; i <= endIndex; i++) {
            ObjPos point = hexagonPoint(size, inset, centered, i);
            path.lineTo(point.x, point.y);
        }
        path.lineTo(endPoint.x, endPoint.y);
        return path;
    }

    public Path2D.Float getPath() {
        return path;
    }
}
