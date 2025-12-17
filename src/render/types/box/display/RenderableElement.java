package render.types.box.display;

import foundation.input.ButtonClickHandler;
import foundation.math.HitBox;
import render.GameRenderer;
import render.HorizontalAlign;
import render.Renderable;

import java.awt.*;

public class RenderableElement extends BoxElement {
    public Renderable renderable;
    public final HorizontalAlign align;
    public boolean enabled = true;
    public final boolean maxWidth;
    public float width, height;

    public RenderableElement(float initialMargin, float width, float height, HorizontalAlign align, Renderable renderable) {
        super(initialMargin);
        this.align = align;
        this.maxWidth = width == -1;
        this.width = width;
        this.height = height;
        this.renderable = renderable;
    }

    @Override
    public float width() {
        return width;
    }

    @Override
    public float height() {
        return height;
    }

    @Override
    public HorizontalAlign align() {
        return align;
    }

    @Override
    public void render(Graphics2D g) {
        GameRenderer.renderOffset(xOffset(), yOffset(), g, () -> renderable.render(g));
    }

    private float xOffset() {
        return 0;
    }

    private float yOffset() {
        return -height();
    }

    @Override
    public boolean maxWidth() {
        return maxWidth;
    }

    @Override
    public void setWidth(float width) {
        if (maxWidth)
            this.width = width;
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

    }
}
