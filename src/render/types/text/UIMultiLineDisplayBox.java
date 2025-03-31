package render.types.text;

import render.*;
import render.types.box.UIBox;

import java.util.function.Consumer;

public class UIMultiLineDisplayBox extends AbstractRenderElement {
    public final float x, y, height, width;
    protected final UIBox box;
    protected final MultiLineTextBox text;

    public UIMultiLineDisplayBox(RenderRegister<OrderedRenderable> register, RenderOrder order, float x, float y, float width, float height, float textSize, TextAlign textAlign, Consumer<UIBox> boxModifier) {
        super(register, order);
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        box = new UIBox(width, height);
        boxModifier.accept(box);
        text = new MultiLineTextBox(switch (textAlign) {
            case LEFT -> 0.5f;
            case CENTER -> width / 2;
            case RIGHT -> width - 0.5f;
        }, height - 0.3f - textSize, width - 1, textSize, textAlign);
        renderable = g -> {
            if (!isEnabled())
                return;
            GameRenderer.renderOffset(x, y, g, () -> {
                box.render(g);
                text.render(g);
            });
        };
    }

    public UIMultiLineDisplayBox setText(String s) {
        text.updateText(s);
        return this;
    }
}
