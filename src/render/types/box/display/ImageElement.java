package render.types.box.display;

import foundation.input.ButtonClickHandler;
import foundation.math.HitBox;
import render.GameRenderer;
import render.HorizontalAlign;
import render.texture.ImageRenderer;

import java.awt.*;

public class ImageElement extends BoxElement {
    public ImageRenderer image;
    public final HorizontalAlign align;
    public boolean enabled = true;
    public final boolean maxWidth;
    public float width, height, renderWidth;

    public ImageElement(float initialMargin, float width, float height, float renderWidth, HorizontalAlign align, ImageRenderer image) {
        super(initialMargin);
        this.align = align;
        this.maxWidth = width == -1;
        this.width = width;
        this.height = height;
        this.renderWidth = renderWidth;
        this.image = image;
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
        GameRenderer.renderOffset(xOffset(), yOffset(), g, () -> image.render(g, renderWidth));
    }

    private float xOffset() {
        return width() / 2;
    }

    private float yOffset() {
        return -height() / 2;
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
