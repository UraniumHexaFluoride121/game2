package render.ui.types;

import foundation.input.*;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import render.*;
import render.ui.UIColourTheme;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.function.Function;

import static level.tile.Tile.*;

public class UIShapeButton extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    private Shape renderShape = null;
    public final float x, y, height, width;
    protected final ButtonClickHandler clickHandler;
    protected final UIBox box;
    protected final StaticHitBox hitBox;
    protected final ButtonOrder buttonOrder;
    protected ButtonRegister buttonRegister;

    public UIShapeButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, boolean staySelected) {
        this(register, buttonRegister, order, buttonOrder, x, y, width, height, staySelected, null);
    }

    public UIShapeButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, boolean staySelected, Runnable onClick) {
        super(register, order);
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.buttonOrder = buttonOrder;
        this.buttonRegister = buttonRegister;
        if (buttonRegister != null) {
            buttonRegister.register(this);
        }
        clickHandler = new ButtonClickHandler(InputType.MOUSE_LEFT, staySelected, onClick);
        box = new UIBox(width, height).setClickHandler(clickHandler);
        hitBox = StaticHitBox.createFromOriginAndSize(x, y, width, height);
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
        };
        return this;
    }

    public UIShapeButton noDeselect() {
        clickHandler.noDeselect();
        return this;
    }

    public UIShapeButton toggleMode() {
        clickHandler.toggleMode();
        return this;
    }

    public UIShapeButton deselect() {
        clickHandler.deselect();
        return this;
    }

    public UIShapeButton setColourTheme(UIColourTheme colourTheme) {
        box.setColourTheme(colourTheme);
        return this;
    }

    public UIShapeButton setOnDeselect(Runnable runnable) {
        clickHandler.setOnDeselect(runnable);
        return this;
    }

    public UIShapeButton setOnClick(Runnable runnable) {
        clickHandler.setOnClick(runnable);
        return this;
    }

    public UIShapeButton setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    public UIShapeButton select() {
        clickHandler.select();
        return this;
    }

    public UIShapeButton setShape(Function<UIBox, Shape> shapeGenerator) {
        renderShape = shapeGenerator.apply(box);
        return this;
    }

    public UIShapeButton setBoxShape(UIBox.BoxShape shape) {
        box.setShape(shape);
        return this;
    }

    public UIShapeButton setBoxCorner(float corner) {
        box.setCorner(corner);
        return this;
    }

    public boolean isSelected() {
        return clickHandler.isSelected();
    }

    @Override
    public boolean posInside(ObjPos pos) {
        return isEnabled() && hitBox.isPositionInside(pos);
    }

    @Override
    public boolean blocking(InputType type) {
        return type.isMouseInput();
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return buttonOrder;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (isEnabled())
            clickHandler.buttonPressed(pos, inside, blocked, type);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (isEnabled())
            clickHandler.buttonReleased(pos, inside, blocked, type);
    }

    @Override
    public void delete() {
        super.delete();
        clickHandler.delete();
        box.setClickHandler(null);
        if (buttonRegister != null) {
            buttonRegister.remove(this);
            buttonRegister = null;
        }
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

    public static Shape plus(UIBox b) {
        float cx = b.width / 2, cy = b.height / 2, size = Math.min(cx, cy) * 0.7f, width = size * .3f;
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
        return path;
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
