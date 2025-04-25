package render.types.text;

import foundation.Deletable;
import foundation.input.TooltipHolder;
import render.Renderable;
import render.types.box.UIBox;

import java.awt.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class TooltipManager implements Renderable, Deletable {
    private TooltipHolder button;
    private UITooltip tooltip;

    public TooltipManager(TooltipHolder button) {
        this.button = button;
    }

    public TooltipManager add(float width, Consumer<UIBox> boxModifier, String text) {
        tooltip = new UITooltip(width == -1 ? 100 : width, 0.6f, TextAlign.LEFT, boxModifier, button)
                .setText(text);
        return this;
    }

    public TooltipManager setText(String text) {
        tooltip.setText(text);
        return this;
    }

    public TooltipManager showIf(BooleanSupplier predicate) {
        tooltip.setPredicate(predicate);
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        if (tooltip != null)
            tooltip.render(g);
    }

    @Override
    public void delete() {
        button = null;
        if (tooltip != null) {
            tooltip.delete();
            tooltip = null;
        }
    }

    public static void hide(TooltipManager t) {
        t.setText(null);
    }
}
