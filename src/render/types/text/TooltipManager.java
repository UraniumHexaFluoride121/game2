package render.types.text;

import foundation.Deletable;
import foundation.input.TooltipHolder;
import render.HorizontalAlign;
import render.Renderable;
import render.types.box.UIBox;

import java.awt.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public class TooltipManager implements Renderable, Deletable {
    private TooltipHolder button;
    private AbstractUITooltip tooltip;

    public TooltipManager(TooltipHolder button) {
        this.button = button;
    }

    public TooltipManager add(float width, Consumer<UIBox> boxModifier, String text) {
        tooltip = new UIMouseTooltip(width == -1 ? 100 : width, 0.6f, HorizontalAlign.LEFT, boxModifier, button)
                .setText(text);
        return this;
    }

    public TooltipManager add(Function<TooltipHolder, AbstractUITooltip> tooltip) {
        this.tooltip = tooltip.apply(button);
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

    public void forceShowTooltip() {
        tooltip.forceShow();
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
        t.tooltip.setEnabled(false);
    }

    public static void show(TooltipManager t) {
        t.tooltip.setEnabled(true);
    }
}
