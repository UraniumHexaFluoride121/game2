package render.types.box.display;

import foundation.input.ButtonClickHandler;
import foundation.math.HitBox;
import render.HorizontalAlign;

import java.awt.*;

public class BoxElementSpace extends BoxElement {
    private final float height;
    public boolean enabled = true;

    public BoxElementSpace(float height) {
        super(0);
        this.height = height;
    }

    @Override
    public float width() {
        return 0;
    }

    @Override
    public float height() {
        return height;
    }

    @Override
    public HorizontalAlign align() {
        return HorizontalAlign.LEFT;
    }

    @Override
    public void render(Graphics2D g) {

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
        return null;
    }

    @Override
    public void setClickHandler(ButtonClickHandler clickHandler) {
        throw new UnsupportedOperationException();
    }
}
