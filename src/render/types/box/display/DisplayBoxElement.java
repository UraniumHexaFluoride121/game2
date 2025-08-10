package render.types.box.display;

import foundation.input.ButtonClickHandler;
import foundation.math.HitBox;
import render.GameRenderer;
import render.HorizontalAlign;
import render.types.box.UIDisplayBox;

import java.awt.*;

public class DisplayBoxElement extends BoxElement {
    public final UIDisplayBox box;
    public final HorizontalAlign align;
    public boolean enabled = true;

    public DisplayBoxElement(float initialMargin, UIDisplayBox box, HorizontalAlign align, Runnable onUpdate) {
        super(initialMargin);
        this.box = box;
        this.align = align;
        box.addOnUpdate(onUpdate);
    }

    @Override
    public float width() {
        return box.width;
    }

    @Override
    public float height() {
        return box.height;
    }

    @Override
    public HorizontalAlign align() {
        return align;
    }

    @Override
    public void render(Graphics2D g) {
        GameRenderer.renderOffset(xOffset(), yOffset(), g, () -> box.render(g));
    }

    private float xOffset() {
        return switch (align) {
            case LEFT -> 0;
            case CENTER -> -width() / 2;
            case RIGHT -> -width();
        };
    }

    private float yOffset() {
        return -height();
    }

    @Override
    public void attemptUpdate(Graphics2D g) {
        box.attemptUpdate(g);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setIsEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public HitBox renderBox() {
        return HitBox.createFromOriginAndSize(xOffset(), yOffset(), width(), height());
    }

    @Override
    public void setClickHandler(ButtonClickHandler clickHandler) {
        box.setClickHandler(clickHandler);
    }

    @Override
    public void delete() {
        box.delete();
    }
}
