package render.types.input;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.UIColourTheme;
import render.types.text.TooltipManager;

import java.util.function.Consumer;

public class UINumberSelector extends AbstractValueSelector<Integer> {
    public final int min, max;

    public UINumberSelector(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float height, float displayWidth, int min, int max, int initialValue) {
        super(register, buttonRegister, order, buttonOrder, x, y, height, displayWidth);
        this.min = min;
        this.max = max;
        init(indexOf(initialValue));
    }

    @Override
    public UINumberSelector tooltip(Consumer<TooltipManager> action) {
        super.tooltip(action);
        return this;
    }

    @Override
    public UINumberSelector setOnChanged(Runnable onChanged) {
        super.setOnChanged(onChanged);
        return this;
    }

    @Override
    public UINumberSelector setCorner(float corner) {
        super.setCorner(corner);
        return this;
    }

    @Override
    public UINumberSelector setDisplayTheme(UIColourTheme displayTheme) {
        super.setDisplayTheme(displayTheme);
        return this;
    }

    @Override
    public UINumberSelector setButtonTheme(UIColourTheme theme) {
        super.setButtonTheme(theme);
        return this;
    }

    @Override
    protected Integer getItem(int index) {
        return index + min;
    }

    @Override
    protected int indexOf(Integer item) {
        return item - min;
    }

    @Override
    protected String getName(Integer item) {
        return String.valueOf(item);
    }

    @Override
    protected int maxIndex() {
        return max - min;
    }
}
