package render.types.text;

import render.AbstractRenderElement;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.types.box.UIBox;

import java.util.function.Consumer;

public class UIMLTextBoxRenderElement extends AbstractRenderElement {
    private UIMultiLineDisplayBox text;

    public UIMLTextBoxRenderElement(RenderRegister<OrderedRenderable> register, RenderOrder order, float x, float y, float width, float height, float textSize, TextAlign textAlign, Consumer<UIBox> boxModifier, boolean dynamicWidth) {
        super(register, order);
        text = new UIMultiLineDisplayBox(x, y, width, height, textSize, textAlign, boxModifier, dynamicWidth);
        renderable = g -> {
            if (isEnabled())
                text.render(g);
        };
    }

    public UIMLTextBoxRenderElement setBoxAlign(TextAlign boxAlign) {
        text.setBoxAlign(boxAlign);
        return this;
    }

    public UIMLTextBoxRenderElement setText(String s) {
        text.setText(s);
        return this;
    }

    @Override
    public void delete() {
        super.delete();
        text.delete();
    }
}
