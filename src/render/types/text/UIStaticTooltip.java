package render.types.text;

import foundation.input.TooltipHolder;
import render.HorizontalAlign;
import render.types.box.UIBox;

import java.awt.*;
import java.util.function.Consumer;

public class UIStaticTooltip extends AbstractUITooltip {
    public UIStaticTooltip(float x, float y, float width, float height, Consumer<UIBox> boxModifier, boolean dynamicWidth, TooltipHolder button) {
        super(x, y, width, height, boxModifier, dynamicWidth, button);
    }

    @Override
    public void render(Graphics2D g) {
        if (readyToRender())
            super.render(g);
    }
}
