package render.types.text;

import foundation.Deletable;
import render.*;
import render.types.box.UIBox;

import java.awt.*;
import java.util.function.Consumer;

public class UIMultiLineDisplayBox implements Renderable, Deletable {
    public final float x, y, textSize;
    public float width, height;
    protected UIBox box;
    protected final MultiLineTextBox text;

    public UIMultiLineDisplayBox(float x, float y, float width, float height, float textSize, TextAlign textAlign, Consumer<UIBox> boxModifier, boolean dynamicWidth) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textSize = textSize;
        box = new UIBox(width, height);
        boxModifier.accept(box);
        float widthMargin = box.corner;
        text = new MultiLineTextBox(switch (textAlign) {
            case LEFT -> widthMargin;
            case CENTER -> width / 2;
            case RIGHT -> width - widthMargin;
        }, 0, width - widthMargin * 2, textSize, textAlign);
        if (height == -1 && dynamicWidth)
            text.setOnUpdate(() -> {
                setHeightToTextSize();
                setWidthToTextSize();
            });
        else if (height == -1)
            text.setOnUpdate(this::setHeightToTextSize);
        else if (dynamicWidth)
            text.setOnUpdate(this::setWidthToTextSize);
    }

    public UIMultiLineDisplayBox setText(String s) {
        text.updateText(s);
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        text.attemptUpdate(g);
        GameRenderer.renderOffset(x, y, g, () -> {
            box.render(g);
            g.translate(0, height - textSize * 0.75f - box.corner);
            text.render(g);
        });
    }

    private void setHeightToTextSize() {
        height = (text.rows() - 0.15f) * text.textSize + box.corner * 2;
        box.setHeight(height);
    }

    private void setWidthToTextSize() {
        width = text.getTextWidth() + box.corner * 2;
        box.setWidth(width);
    }

    @Override
    public void delete() {
        text.delete();
    }
}
