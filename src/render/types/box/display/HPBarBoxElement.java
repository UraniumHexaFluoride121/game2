package render.types.box.display;

import foundation.input.ButtonClickHandler;
import foundation.math.HitBox;
import render.GameRenderer;
import render.HorizontalAlign;
import render.types.UIHitPointBar;

import java.awt.*;

public class HPBarBoxElement extends BoxElement {
    public final UIHitPointBar bar;
    private final HorizontalAlign align;
    public boolean enabled = true;

    public HPBarBoxElement(float initialMargin, UIHitPointBar bar, HorizontalAlign align) {
        super(initialMargin);
        this.bar = bar;
        this.align = align;
    }

    @Override
    public float width() {
        return bar.barOnly ? bar.getTotalBarWidth() : bar.width;
    }

    @Override
    public float height() {
        return bar.barOnly ? bar.getSegmentHeight() : bar.height;
    }

    @Override
    public boolean maxWidth() {
        return false;
    }

    @Override
    public void setWidth(float width) {
        bar.setWidth(width);
    }

    @Override
    public HorizontalAlign align() {
        return align;
    }

    @Override
    public void render(Graphics2D g) {
        GameRenderer.renderOffset(xOffset(), yOffset(), g, () -> bar.render(g));
    }

    private float xOffset() {
        return switch (align) {
            case LEFT -> 0;
            case CENTER -> -width() / 2;
            case RIGHT -> -width();
        } - (bar.barOnly ? bar.spacing : 0);
    }

    private float yOffset() {
        return bar.barOnly ? -bar.height + bar.spacing : -bar.height;
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
        throw new UnsupportedOperationException();
    }
}
