package render.types.text;

import foundation.input.TooltipHolder;
import render.UIColourTheme;
import render.types.box.UIBox;
import render.types.box.UIDisplayBox;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public abstract class AbstractUITooltip extends UIDisplayBox {
    protected TooltipHolder button;
    protected BooleanSupplier predicate = null;
    protected long firstHover = -1;
    protected boolean prevRender = false;
    protected long prevUpdateTime = 0;
    protected Runnable onStartRender = null;

    public AbstractUITooltip(float x, float y, float width, float height, Consumer<UIBox> boxModifier, boolean dynamicWidth, TooltipHolder button) {
        super(x, y, width, height, boxModifier, dynamicWidth);
        this.button = button;
    }

    public AbstractUITooltip setText(String s) {
        super.setText(s);
        return this;
    }

    public AbstractUITooltip setPredicate(BooleanSupplier predicate) {
        this.predicate = predicate;
        return this;
    }

    protected void forceShow() {
        firstHover = System.currentTimeMillis() - 400;
    }

    protected boolean readyToRender() {
        boolean render = true;
        long time = System.currentTimeMillis();
        if (firstHover == -1) {
            if (button.getClickHandler().mouseHover)
                firstHover = time;
            else {
                render = false;
            }
        } else if (!button.getClickHandler().mouseHover) {
            firstHover = -1;
            render = false;
        }
        if (render) {
            if (predicate != null && !predicate.getAsBoolean()) {
                firstHover = time;
                render = false;
            } else {
                render = firstHover <= time - 400;
            }
        }
        if (onStartRender != null && render && (!prevRender || prevUpdateTime <= time - 1000)) {
            prevUpdateTime = time;
            onStartRender.run();
        }
        prevRender = render;
        return render;
    }

    public AbstractUITooltip setOnStartRender(Runnable onStartRender) {
        this.onStartRender = onStartRender;
        return this;
    }

    public void forceStartRenderUpdate() {
        if (onStartRender != null)
            onStartRender.run();
    }

    @Override
    public void delete() {
        super.delete();
        button = null;
        predicate = null;
        onStartRender = null;
    }

    public static Consumer<UIBox> dark() {
        return b -> b.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX_DARK).setCorner(0.45f);
    }

    public static Consumer<UIBox> light() {
        return b -> b.setColourTheme(UIColourTheme.LIGHT_BLUE_BOX).setCorner(0.45f);
    }
}
