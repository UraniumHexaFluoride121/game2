package render.ui.types;

import foundation.input.*;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import render.*;
import render.ui.UIColourTheme;

import java.awt.*;
import java.util.function.Function;

import static level.Tile.*;

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
            if (!enabled)
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

    public UIShapeButton noDeselect() {
        clickHandler.noDeselect();
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

    public UIShapeButton setShape(Function<UIShapeButton, Shape> shapeGenerator) {
        renderShape = shapeGenerator.apply(this);
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

    @Override
    public boolean posInside(ObjPos pos) {
        return enabled && hitBox.isPositionInside(pos);
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
        if (enabled)
            clickHandler.buttonPressed(pos, inside, blocked, type);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (enabled)
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

    public static Shape triangleLeft(UIShapeButton b) {
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

    public static Shape triangleRight(UIShapeButton b) {
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

    public static Shape plus(UIShapeButton b) {
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

    public static Shape x(UIShapeButton b) {
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
}
