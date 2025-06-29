package render.types.text;

import foundation.MainPanel;
import foundation.input.TooltipHolder;
import render.GameRenderer;
import render.HorizontalAlign;
import render.Renderable;
import render.types.box.UIBox;

import java.awt.*;
import java.util.function.Consumer;

public class UIMouseTooltip extends AbstractUITooltip {
    private static final float X_OFFSET = 0.3f, Y_OFFSET = 0.4f;

    public UIMouseTooltip(float width, float textSize, HorizontalAlign textAlign, Consumer<UIBox> boxModifier, TooltipHolder button) {
        super(0, 0, width, -1, boxModifier, true, button);
        addText(textSize, textAlign, null);
    }

    @Override
    public void render(Graphics2D g) {
        if (readyToRender())
            MainPanel.generatedTooltipRenderers.add(g2 -> {
                GameRenderer.renderOffset(MainPanel.lastUIMousePos, g2, () -> {
                    g2.translate(X_OFFSET, Y_OFFSET);
                    g2.translate(-Math.max(0, MainPanel.lastUIMousePos.x + width + 0.3f + X_OFFSET - Renderable.right()), 0);
                    if (MainPanel.lastUIMousePos.y + height + 0.3f + Y_OFFSET > Renderable.top())
                        g2.translate(0, -height - Y_OFFSET * 2 - 0.3f);
                    super.render(g2);
                });
            });
    }

}
