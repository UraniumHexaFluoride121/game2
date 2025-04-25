package render.types.text;

import foundation.MainPanel;
import foundation.input.TooltipHolder;
import render.GameRenderer;
import render.Renderable;
import render.UIColourTheme;
import render.types.box.UIBox;

import java.awt.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class UITooltip extends UIMultiLineDisplayBox {
    private static final float X_OFFSET = 0.3f, Y_OFFSET = 0.4f;
    private TooltipHolder button;
    private BooleanSupplier predicate = null;
    private long firstHover = -1;

    public UITooltip(float width, float textSize, TextAlign textAlign, Consumer<UIBox> boxModifier, TooltipHolder button) {
        super(0, 0, width, -1, textSize, textAlign, boxModifier, true);
        this.button = button;
    }

    public UITooltip setText(String s) {
        text.updateText(s);
        return this;
    }

    public UITooltip setPredicate(BooleanSupplier predicate) {
        this.predicate = predicate;
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        if (firstHover == -1) {
            if (button.getClickHandler().mouseHover)
                firstHover = System.currentTimeMillis();
            else
                return;
        } else if (!button.getClickHandler().mouseHover) {
            firstHover = -1;
            return;
        }
        if (text.isEmpty() || (predicate != null && !predicate.getAsBoolean())) {
            text.attemptUpdate(g);
            firstHover = System.currentTimeMillis();
            return;
        }
        if (firstHover > System.currentTimeMillis() - 400)
            return;
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

    @Override
    public void delete() {
        super.delete();
        button = null;
        predicate = null;
    }

    public static Consumer<UIBox> dark() {
        return b -> b.setColourTheme(UIColourTheme.LIGHT_BLUE_OPAQUE_CENTER).setCorner(0.35f);
    }

    public static Consumer<UIBox> light() {
        return b -> b.setColourTheme(UIColourTheme.LIGHT_BLUE_OPAQUE_CENTER_LIGHT).setCorner(0.35f);
    }
}
