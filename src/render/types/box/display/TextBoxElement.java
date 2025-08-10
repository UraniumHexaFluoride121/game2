package render.types.box.display;

import foundation.input.ButtonClickHandler;
import foundation.math.HitBox;
import render.HorizontalAlign;
import render.types.text.MultiLineTextBox;

import java.awt.*;

public class TextBoxElement extends BoxElement {
    public final MultiLineTextBox text;
    public HorizontalAlign align;
    public boolean enabled = true;
    private boolean dynamicWidth;

    public TextBoxElement(float initialMargin, float width, float textSize, HorizontalAlign align, String s, Runnable onUpdate, boolean dynamicWidth) {
        super(initialMargin);
        this.align = align;
        this.dynamicWidth = dynamicWidth;
        text = new MultiLineTextBox(0, 0, width, textSize, align).updateText(s).setOnUpdate(onUpdate);
    }

    public TextBoxElement setAlign(HorizontalAlign align) {
        this.align = align;
        text.setTextAlign(align);
        return this;
    }

    public TextBoxElement dynamicWidth(boolean dynamicWidth) {
        this.dynamicWidth = dynamicWidth;
        return this;
    }

    @Override
    public float width() {
        return text.getTextWidth();
    }

    @Override
    public float height() {
        return (text.rows() - 0.15f) * text.textSize;
    }

    @Override
    public void setWidth(float width) {
        if (dynamicWidth)
            text.setWidth(width);
    }

    @Override
    public HorizontalAlign align() {
        return align;
    }

    @Override
    public void render(Graphics2D g) {
        g.translate(0, -text.textSize * 0.75f);
        text.render(g);
    }

    @Override
    public void attemptUpdate(Graphics2D g) {
        text.attemptUpdate(g);
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
        return HitBox.createFromOriginAndSize(0, 0, width(), height());
    }

    @Override
    public void setClickHandler(ButtonClickHandler clickHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete() {
        text.delete();
    }
}
