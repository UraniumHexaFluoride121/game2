package render.types.box;

import render.*;

import java.util.function.Consumer;

public class UIDisplayBoxRenderElement extends AbstractRenderElement {
    public UIDisplayBox box;

    public UIDisplayBoxRenderElement(RenderRegister<OrderedRenderable> register, RenderOrder order, float x, float y, float width, float height, Consumer<UIBox> boxModifier, boolean dynamicWidth) {
        super(register, order);
        box = new UIDisplayBox(x, y, width, height, boxModifier, dynamicWidth);
        renderable = g -> {
            if (isEnabled())
                box.render(g);
        };
    }

    public UIDisplayBoxRenderElement addText(float textSize, HorizontalAlign align, String s) {
        box.addText(textSize, align, s);
        return this;
    }

    @Override
    public void delete() {
        super.delete();
        box.delete();
    }
}
