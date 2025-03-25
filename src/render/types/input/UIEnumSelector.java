package render.types.input;

import foundation.NamedEnum;
import foundation.input.*;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import render.AbstractRenderElement;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.types.input.button.UIShapeButton;
import render.UIColourTheme;
import render.types.box.UIBox;
import render.types.text.UITextDisplayBox;

public class UIEnumSelector<T extends NamedEnum> extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    private ButtonRegister buttonRegister, internal = new ButtonRegister();
    private UIShapeButton left, right;
    private UITextDisplayBox displayBox;
    private final ButtonOrder buttonOrder;
    private final float x, y, height, displayWidth;
    private T value;
    private final T[] items;
    private int index;
    private UIColourTheme theme = UIColourTheme.GREEN_SELECTED, displayTheme = UIColourTheme.GREEN_SELECTED;
    private Runnable onChanged = null;
    private final StaticHitBox hitBox;

    public UIEnumSelector(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float height, float displayWidth, Class<T> enumClass, T initialValue) {
        this(register, buttonRegister, order, buttonOrder, x, y, height, displayWidth, enumClass.getEnumConstants(), ((Enum<?>) initialValue).ordinal());
    }

    public UIEnumSelector(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float height, float displayWidth, T[] items, int initialIndex) {
        super(register, order);
        this.buttonRegister = buttonRegister;
        this.buttonOrder = buttonOrder;
        this.x = x;
        this.y = y;
        this.height = height;
        this.displayWidth = displayWidth;
        index = initialIndex;
        this.items = items;
        value = items[index];
        hitBox = StaticHitBox.createFromOriginAndSize(x, y, 2 * height + .5f * 2 + displayWidth, height);
        left = new UIShapeButton(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, x, y, height, height, false)
                .setColourTheme(theme)
                .setShape(UIShapeButton::triangleLeft)
                .setBoxShape(UIBox.BoxShape.RECTANGLE_LEFT_CORNERS_CUT)
                .setOnClick(this::decrement);
        right = new UIShapeButton(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, x + height + .5f * 2 + displayWidth, y, height, height, false)
                .setColourTheme(theme)
                .setShape(UIShapeButton::triangleRight)
                .setBoxShape(UIBox.BoxShape.RECTANGLE_RIGHT_CORNERS_CUT)
                .setOnClick(this::increment);
        displayBox = new UITextDisplayBox(null, RenderOrder.NONE, x + height + .5f, y, displayWidth, height, height * 0.8f)
                .setBold().setBoxShape(UIBox.BoxShape.RECTANGLE).setText(value.getName()).setColourTheme(displayTheme);
        renderable = g -> {
            left.render(g);
            right.render(g);
            displayBox.render(g);
        };
        if (buttonRegister != null)
            buttonRegister.register(this);
        updateColour();
    }

    public float totalWidth() {
        return 2 * height + .5f * 2 + displayWidth;
    }

    public static float totalWidth(float height, float displayWidth) {
        return 2 * height + .5f * 2 + displayWidth;
    }

    private void increment() {
        T prevValue = value;
        index = Math.min(index + 1, items.length - 1);
        value = items[index];
        displayBox.setText(value.getName());
        updateColour();
        if (onChanged != null && prevValue != value)
            onChanged.run();
    }

    private void decrement() {
        T prevValue = value;
        index = Math.max(index - 1, 0);
        value = items[index];
        displayBox.setText(value.getName());
        updateColour();
        if (onChanged != null && prevValue != value)
            onChanged.run();
    }

    private void updateColour() {
        left.setColourTheme(index == 0 ? UIColourTheme.GRAYED_OUT : theme);
        right.setColourTheme(index == items.length - 1 ? UIColourTheme.GRAYED_OUT : theme);
        displayBox.setColourTheme(displayTheme);
    }

    public UIEnumSelector<T> setDisplayTheme(UIColourTheme displayTheme) {
        this.displayTheme = displayTheme;
        updateColour();
        return this;
    }

    public UIEnumSelector<T> setButtonTheme(UIColourTheme theme) {
        this.theme = theme;
        updateColour();
        return this;
    }

    public T getValue() {
        return value;
    }

    public UIEnumSelector<T> setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged;
        return this;
    }

    public UIEnumSelector<T> setCorner(float corner) {
        left.setBoxCorner(corner);
        right.setBoxCorner(corner);
        return this;
    }

    @Override
    public boolean posInside(ObjPos pos) {
        return isEnabled() && hitBox.isPositionInside(pos);
    }

    private boolean blocking = false;

    @Override
    public boolean blocking(InputType type) {
        return blocking;
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return buttonOrder;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (isEnabled()) {
            if (!blocked && inside && type instanceof ScrollInputType s) {
                if (s.up) {
                    increment();
                } else
                    decrement();
                blocking = true;
            } else
                blocking = internal.acceptInput(pos, type, true, blocked);
        }
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (isEnabled())
            blocking = internal.acceptInput(pos, type, false, blocked);
    }

    @Override
    public void delete() {
        super.delete();
        if (buttonRegister != null) {
            buttonRegister.remove(this);
            buttonRegister = null;
        }
        left.delete();
        right.delete();
        displayBox.delete();
        internal.delete();
        onChanged = null;
    }
}
