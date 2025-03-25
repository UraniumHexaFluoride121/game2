package render.types.text;

import render.*;
import render.types.box.UIBox;
import render.UIColourTheme;

public class UITextDisplayBox extends AbstractRenderElement {
    protected final FixedTextRenderer text;
    public final float x, y, height, width;
    protected final UIBox box;

    public UITextDisplayBox(RenderRegister<OrderedRenderable> register, RenderOrder order, float x, float y, float width, float height, float textSize) {
        super(register, order);
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        box = new UIBox(width, height);
        text = new FixedTextRenderer(null, textSize, UITextLabel.TEXT_COLOUR)
                .setTextAlign(TextAlign.CENTER);
        renderable = g -> {
            if (!isEnabled())
                return;
            GameRenderer.renderOffset(x, y, g, () -> {
                box.render(g);
                g.translate(width / 2f, height / 2 - textSize * 0.75 / 2);
                text.render(g);
            });
        };
    }

    public UITextDisplayBox setText(String text) {
        this.text.updateText(text);
        return this;
    }

    public UITextDisplayBox setBold() {
        text.setBold(true);
        return this;
    }

    public UITextDisplayBox setColourTheme(UIColourTheme colourTheme) {
        box.setColourTheme(colourTheme);
        return this;
    }

    public UITextDisplayBox setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    public UITextDisplayBox setBoxShape(UIBox.BoxShape shape) {
        box.setShape(shape);
        return this;
    }
}
