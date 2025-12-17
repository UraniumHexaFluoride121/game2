package render.types.box.display;

import foundation.Deletable;
import foundation.input.ButtonClickHandler;
import foundation.math.HitBox;
import render.HorizontalAlign;

import java.awt.*;

public abstract class BoxElement implements Deletable {
    public float leftMargin, rightMargin;

    public BoxElement(float initialMargin) {
        leftMargin = initialMargin;
        rightMargin = initialMargin;
    }

    public void setMargin(float margin) {
        leftMargin = margin;
        rightMargin = margin;
    }

    public abstract boolean maxWidth();

    public void setWidth(float width) {

    }

    public void attemptUpdate(Graphics2D g) {

    }

    @Override
    public void delete() {

    }

    public abstract float width();

    public abstract float height();

    public abstract HorizontalAlign align();

    public abstract void render(Graphics2D g);

    public abstract boolean isEnabled();

    public abstract void setIsEnabled(boolean enabled);

    public abstract HitBox renderBox();

    public abstract void setClickHandler(ButtonClickHandler clickHandler);
}
