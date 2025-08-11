package render.types.input.button;

import foundation.input.ButtonRegister;
import render.*;
import render.types.box.UIBox;
import render.types.text.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.function.Consumer;
import java.util.function.Function;

import static level.tile.Tile.*;

public class UIShapeButton extends AbstractUIButton {
    private Shape renderShape = null;

    public UIShapeButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, float x, float y, float width, float height, boolean staySelected) {
        this(register, buttonRegister, order, x, y, width, height, staySelected, null);
    }

    public UIShapeButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, float x, float y, float width, float height, boolean staySelected, Runnable onClick) {
        super(register, buttonRegister, order, x, y, height, width, staySelected, onClick);
        renderable = g -> {
            if (!isEnabled())
                return;
            GameRenderer.renderOffset(x, y, g, () -> {
                box.render(g);
                g.setColor(UITextLabel.TEXT_COLOUR);
                if (renderShape != null) {
                    GameRenderer.renderScaled(1f / SCALING, g, () -> {
                        g.fill(renderShape);
                    });
                }
            });
            tooltip.render(g);
        };
    }

    public UIShapeButton drawShape(float width) {
        BasicStroke stroke = Renderable.roundedStroke(width * SCALING);
        renderable = g -> {
            if (!isEnabled())
                return;
            GameRenderer.renderOffset(x, y, g, () -> {
                box.render(g);
                g.setColor(UITextLabel.TEXT_COLOUR);
                if (renderShape != null) {
                    GameRenderer.renderScaled(1f / SCALING, g, () -> {
                        g.setStroke(stroke);
                        g.draw(renderShape);
                    });
                }
            });
            tooltip.render(g);
        };
        return this;
    }

    public UIShapeButton textRenderable(TextRenderable textRenderable, float size) {
        TextRenderElement renderElement = textRenderable.renderable.apply(new TextStyle().setColour(UITextLabel.TEXT_COLOUR));
        float scale = Math.min(width / renderElement.width, height / 20) * size;
        float x = width / 2 - renderElement.width * scale / 2;
        float y = height / 2 - 15 * scale / 2;
        renderable = g -> {
            if (!isEnabled())
                return;
            GameRenderer.renderOffset(this.x, this.y, g, () -> {
                box.render(g);
                g.translate(x, y);
                g.scale(scale, -scale);
                renderElement.renderable.render(g);
            });
            tooltip.render(g);
        };
        return this;
    }

    @Override
    public UIShapeButton tooltip(Consumer<TooltipManager> action) {
        super.tooltip(action);
        return this;
    }

    @Override
    public UIShapeButton noDeselect() {
        super.noDeselect();
        return this;
    }

    @Override
    public UIShapeButton toggleMode() {
        super.toggleMode();
        return this;
    }

    @Override
    public UIShapeButton deselect() {
        super.deselect();
        return this;
    }

    @Override
    public UIShapeButton setColourTheme(UIColourTheme colourTheme) {
        super.setColourTheme(colourTheme);
        return this;
    }

    @Override
    public UIShapeButton setOnDeselect(Runnable runnable) {
        super.setOnDeselect(runnable);
        return this;
    }

    @Override
    public UIShapeButton setOnClick(Runnable runnable) {
        super.setOnClick(runnable);
        return this;
    }

    @Override
    public UIShapeButton setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    @Override
    public UIShapeButton setClickEnabled(boolean clickEnabled) {
        super.setClickEnabled(true);
        return this;
    }

    @Override
    public UIShapeButton select() {
        super.select();
        return this;
    }

    public UIShapeButton setShape(Function<UIBox, Shape> shapeGenerator) {
        renderShape = shapeGenerator.apply(box);
        return this;
    }

    @Override
    public UIShapeButton setBoxShape(UIBox.BoxShape shape) {
        super.setBoxShape(shape);
        return this;
    }

    @Override
    public UIShapeButton setBoxCorner(float corner) {
        super.setBoxCorner(corner);
        return this;
    }

    public static Shape triangleLeft(UIBox b) {
        float cx = b.width / 2, cy = b.height / 2, size = Math.min(cx, cy) * 0.7f;
        return new Polygon(new int[]{
                (int) ((-size + cx + size / 10) * SCALING),
                (int) ((size / 2 + cx + size / 10) * SCALING),
                (int) ((size / 2 + cx + size / 10) * SCALING),
        }, new int[]{
                (int) (cy * SCALING),
                (int) ((size * SIN_60_DEG + cy) * SCALING),
                (int) ((-size * SIN_60_DEG + cy) * SCALING),
        }, 3);
    }

    public static Shape triangleRight(UIBox b) {
        float cx = b.width / 2, cy = b.height / 2, size = Math.min(cx, cy) * 0.7f;
        return new Polygon(new int[]{
                (int) ((size + cx - size / 10) * SCALING),
                (int) ((-size / 2 + cx - size / 10) * SCALING),
                (int) ((-size / 2 + cx - size / 10) * SCALING),
        }, new int[]{
                (int) (cy * SCALING),
                (int) ((size * SIN_60_DEG + cy) * SCALING),
                (int) ((-size * SIN_60_DEG + cy) * SCALING),
        }, 3);
    }

    public static Shape plus(UIBox b, float sizeMultiplier, float widthMultiplier) {
        float cx = b.width / 2, cy = b.height / 2, size = Math.min(cx, cy) * sizeMultiplier, width = size * widthMultiplier;
        return new Polygon(new int[]{
                (int) ((cx - size) * SCALING),
                (int) ((cx - size) * SCALING),
                (int) ((cx - width) * SCALING),
                (int) ((cx - width) * SCALING),
                (int) ((cx + width) * SCALING),
                (int) ((cx + width) * SCALING),
                (int) ((cx + size) * SCALING),
                (int) ((cx + size) * SCALING),
                (int) ((cx + width) * SCALING),
                (int) ((cx + width) * SCALING),
                (int) ((cx - width) * SCALING),
                (int) ((cx - width) * SCALING),
        }, new int[]{
                (int) ((cy - width) * SCALING),
                (int) ((cy + width) * SCALING),
                (int) ((cy + width) * SCALING),
                (int) ((cy + size) * SCALING),
                (int) ((cy + size) * SCALING),
                (int) ((cy + width) * SCALING),
                (int) ((cy + width) * SCALING),
                (int) ((cy - width) * SCALING),
                (int) ((cy - width) * SCALING),
                (int) ((cy - size) * SCALING),
                (int) ((cy - size) * SCALING),
                (int) ((cy - width) * SCALING),
        }, 12);
    }

    public static Path2D.Float plus(float scale) {
        return plus(0.7f * scale, 0.3f * scale);
    }

    public static Path2D.Float plus(float size, float width) {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(-size, -width);
        path.lineTo(-size, width);
        path.lineTo(-width, width);
        path.lineTo(-width, size);
        path.lineTo(width, size);
        path.lineTo(width, width);
        path.lineTo(size, width);
        path.lineTo(size, -width);
        path.lineTo(width, -width);
        path.lineTo(width, -size);
        path.lineTo(-width, -size);
        path.lineTo(-width, -width);
        path.closePath();
        return path;
    }

    public static Shape plus(UIBox b) {
        return plus(b, 0.7f, 0.3f);
    }

    public static Shape minus(UIBox b) {
        float cx = b.width / 2, cy = b.height / 2, size = Math.min(cx, cy) * 0.7f, width = size * .3f;
        return new Polygon(new int[]{
                (int) ((cx - size) * SCALING),
                (int) ((cx - size) * SCALING),
                (int) ((cx + size) * SCALING),
                (int) ((cx + size) * SCALING),
        }, new int[]{
                (int) ((cy - width) * SCALING),
                (int) ((cy + width) * SCALING),
                (int) ((cy + width) * SCALING),
                (int) ((cy - width) * SCALING),
        }, 4);
    }

    public static Shape x(UIBox b) {
        float cx = b.width / 2, cy = b.height / 2, size = Math.min(cx, cy) * 0.7f, thickness = size * .3f;
        return new Polygon(new int[]{
                (int) ((cx - size) * SCALING),
                (int) ((cx - thickness) * SCALING),
                (int) ((cx - size) * SCALING),
                (int) ((cx - size + thickness) * SCALING),
                (int) ((cx) * SCALING),
                (int) ((cx + size - thickness) * SCALING),
                (int) ((cx + size) * SCALING),
                (int) ((cx + thickness) * SCALING),
                (int) ((cx + size) * SCALING),
                (int) ((cx + size - thickness) * SCALING),
                (int) ((cx) * SCALING),
                (int) ((cx - size + thickness) * SCALING),
        }, new int[]{
                (int) ((cy - size + thickness) * SCALING),
                (int) ((cy) * SCALING),
                (int) ((cy + size - thickness) * SCALING),
                (int) ((cy + size) * SCALING),
                (int) ((cy + thickness) * SCALING),
                (int) ((cy + size) * SCALING),
                (int) ((cy + size - thickness) * SCALING),
                (int) ((cy) * SCALING),
                (int) ((cy - size + thickness) * SCALING),
                (int) ((cy - size) * SCALING),
                (int) ((cy - thickness) * SCALING),
                (int) ((cy - size) * SCALING),
        }, 12);
    }

    public static Shape smallX(UIBox b) {
        float cx = b.width / 2, cy = b.height / 2, size = Math.min(cx, cy) * 0.6f, thickness = size * .3f;
        return new Polygon(new int[]{
                (int) ((cx - size) * SCALING),
                (int) ((cx - thickness) * SCALING),
                (int) ((cx - size) * SCALING),
                (int) ((cx - size + thickness) * SCALING),
                (int) ((cx) * SCALING),
                (int) ((cx + size - thickness) * SCALING),
                (int) ((cx + size) * SCALING),
                (int) ((cx + thickness) * SCALING),
                (int) ((cx + size) * SCALING),
                (int) ((cx + size - thickness) * SCALING),
                (int) ((cx) * SCALING),
                (int) ((cx - size + thickness) * SCALING),
        }, new int[]{
                (int) ((cy - size + thickness) * SCALING),
                (int) ((cy) * SCALING),
                (int) ((cy + size - thickness) * SCALING),
                (int) ((cy + size) * SCALING),
                (int) ((cy + thickness) * SCALING),
                (int) ((cy + size) * SCALING),
                (int) ((cy + size - thickness) * SCALING),
                (int) ((cy) * SCALING),
                (int) ((cy - size + thickness) * SCALING),
                (int) ((cy - size) * SCALING),
                (int) ((cy - thickness) * SCALING),
                (int) ((cy - size) * SCALING),
        }, 12);
    }

    public static Shape i(UIBox b) {
        float cx = b.width / 2 * SCALING, cy = b.height / 2 * SCALING, size = Math.min(cx, cy) * 0.7f;
        Path2D.Float path = new Path2D.Float();
        path.moveTo(cx, cy - size * 0.8f);
        path.lineTo(cx, cy + size * 0.1f);
        path.closePath();
        path.moveTo(cx, cy + size * 0.8f);
        path.lineTo(cx, cy + size * 0.801f);
        path.closePath();
        return path;
    }

    public static Shape dot(UIBox b) {
        float cx = b.width / 2 * SCALING, cy = b.height / 2 * SCALING, size = Math.min(cx, cy) * 0.4f;
        return new Ellipse2D.Float(cx - size, cy - size, size * 2, size * 2);
    }

    public static Shape target(UIBox b) {
        return targetRotated(b, 0);
    }

    public static Shape targetRotated(UIBox b, float angle) {
        float cx = b.width / 2 * SCALING, cy = b.height / 2 * SCALING, size = Math.min(cx, cy) * 0.5f;
        Path2D.Float path = new Path2D.Float();
        path.moveTo(cx, cy - size * 1.2f);
        path.lineTo(cx, cy - size * 0.65f);
        path.moveTo(cx, cy + size * 1.2f);
        path.lineTo(cx, cy + size * 0.65f);
        path.moveTo(cx - size * 1.2f, cy);
        path.lineTo(cx - size * 0.65f, cy);
        path.moveTo(cx + size * 1.2f, cy);
        path.lineTo(cx + size * 0.65f, cy);
        Ellipse2D.Float circle = new Ellipse2D.Float(cx - size, cy - size, size * 2, size * 2);
        path.append(circle.getPathIterator(null), false);
        AffineTransform t = new AffineTransform();
        t.rotate(Math.toRadians(angle), cx, cy);
        return path.createTransformedShape(t);
    }


    public static Shape threeLines(UIBox b) {
        float cx = b.width / 2 * SCALING, cy = b.height / 2 * SCALING, size = Math.min(cx, cy) * 0.5f;
        Path2D.Float path = new Path2D.Float();
        path.moveTo(cx - size, cy - size * 0.7f);
        path.lineTo(cx + size, cy - size * 0.7f);
        path.moveTo(cx - size, cy);
        path.lineTo(cx + size, cy);
        path.moveTo(cx - size, cy + size * 0.7f);
        path.lineTo(cx + size, cy + size * 0.7f);
        return path;
    }

    public static Shape map(UIBox b) {
        float cx = b.width / 2 * SCALING, cy = b.height / 2 * SCALING, size = Math.min(cx, cy) * 0.7f;
        Path2D.Float path = new Path2D.Float();
        path.moveTo(cx - size * 0.8f, cy - size * 0.7f);
        path.lineTo(cx - size * 0.8f, cy + size * 0.4f);
        path.lineTo(cx - size * 0.2f, cy + size * 0.7f);
        path.lineTo(cx - size * 0.2f, cy - size * 0.4f);
        path.closePath();
        path.moveTo(cx - size * 0.3f, cy + size * 0.7f);
        path.lineTo(cx - size * 0.3f, cy - size * 0.4f);
        path.lineTo(cx + size * 0.3f, cy - size * 0.7f);
        path.lineTo(cx + size * 0.3f, cy + size * 0.4f);
        path.closePath();
        path.moveTo(cx + size * 0.4f, cy - size * 0.7f);
        path.lineTo(cx + size * 0.4f, cy + size * 0.4f);
        path.lineTo(cx + size * 0.8f, cy + size * 0.7f);
        path.lineTo(cx + size * 0.8f, cy - size * 0.4f);
        path.closePath();
        return path;
    }
}
