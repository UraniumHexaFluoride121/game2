package render.types.input;

import foundation.NamedEnum;
import foundation.input.ButtonRegister;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.UIColourTheme;
import render.types.text.TooltipManager;

import java.util.function.Consumer;

public class UIEnumSelector<T extends NamedEnum> extends AbstractValueSelector<T> {
    private final T[] items;

    public UIEnumSelector(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, float x, float y, float height, float displayWidth, Class<T> enumClass, T initialValue) {
        this(register, buttonRegister, order, x, y, height, displayWidth, enumClass.getEnumConstants(), ((Enum<?>) initialValue).ordinal());
    }

    public UIEnumSelector(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, float x, float y, float height, float displayWidth, T[] items, int initialIndex) {
        super(register, buttonRegister, order, x, y, height, displayWidth);
        this.items = items;
        init(initialIndex);
    }

    @Override
    public UIEnumSelector<T> tooltip(Consumer<TooltipManager> action) {
        super.tooltip(action);
        return this;
    }

    @Override
    public UIEnumSelector<T> setOnChanged(Runnable onChanged) {
        super.setOnChanged(onChanged);
        return this;
    }

    @Override
    public UIEnumSelector<T> setCorner(float corner) {
        super.setCorner(corner);
        return this;
    }

    @Override
    public UIEnumSelector<T> setDisplayTheme(UIColourTheme displayTheme) {
        super.setDisplayTheme(displayTheme);
        return this;
    }

    @Override
    public UIEnumSelector<T> setButtonTheme(UIColourTheme theme) {
        super.setButtonTheme(theme);
        return this;
    }

    @Override
    protected T getItem(int index) {
        return items[index];
    }

    @Override
    protected int indexOf(T item) {
        for (int i = 0; i < maxIndex(); i++) {
            if (items[i] == item)
                return i;
        }
        throw new IllegalArgumentException("Enum selector could not find item: " + getName(item));
    }

    @Override
    protected String getName(T item) {
        return item.getName();
    }

    @Override
    protected int maxIndex() {
        return items.length - 1;
    }
}
