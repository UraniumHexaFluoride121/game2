package render.ui.types;

import foundation.NamedEnum;
import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import render.AbstractRenderElement;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.ui.UIColourTheme;

public class UIEnumSelector<T extends Enum<T> & NamedEnum> extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    private ButtonRegister buttonRegister, internal = new ButtonRegister();
    private UIShapeButton left, right;
    private UITextDisplayBox displayBox;
    private final ButtonOrder buttonOrder;
    private final float x, y, height, displayWidth;
    private T value;
    private final Class<T> enumClass;
    private UIColourTheme theme = UIColourTheme.GREEN_SELECTED;
    private Runnable onChanged = null;
    private final StaticHitBox hitBox;

    public UIEnumSelector(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float height, float displayWidth, Class<T> enumClass, T initialValue) {
        super(register, order);
        this.buttonRegister = buttonRegister;
        this.buttonOrder = buttonOrder;
        this.x = x;
        this.y = y;
        this.height = height;
        this.displayWidth = displayWidth;
        this.enumClass = enumClass;
        value = initialValue;
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
                .setBold().setBoxShape(UIBox.BoxShape.RECTANGLE).setText(String.valueOf(value));
        renderable = g -> {
            left.render(g);
            right.render(g);
            displayBox.render(g);
        };
        if (buttonRegister != null)
            buttonRegister.register(this);
        updateColour();
    }

    private void increment() {
        T prevValue = value;
        value = enumClass.getEnumConstants()[Math.min(value.ordinal() + 1, enumClass.getEnumConstants().length - 1)];
        displayBox.setText(value.getName());
        updateColour();
        if (onChanged != null && prevValue != value)
            onChanged.run();
    }

    private void decrement() {
        T prevValue = value;
        value = enumClass.getEnumConstants()[Math.max(value.ordinal() - 1, 0)];
        displayBox.setText(value.getName());
        updateColour();
        if (onChanged != null && prevValue != value)
            onChanged.run();
    }

    private void updateColour() {
        left.setColourTheme(value.ordinal() == 0 ? UIColourTheme.GRAYED_OUT : theme);
        right.setColourTheme(value.ordinal() == enumClass.getEnumConstants().length - 1 ? UIColourTheme.GRAYED_OUT : theme);
    }

    public T getValue() {
        return value;
    }

    public UIEnumSelector<T> setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged;
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
            if (!blocked && inside && type.isScrollInputOnce()) {
                if (type == InputType.MOUSE_SCROLL_UP_ONCE) {
                    increment();
                } else
                    decrement();
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
