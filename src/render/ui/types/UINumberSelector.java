package render.ui.types;

import foundation.input.*;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import render.AbstractRenderElement;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.ui.UIColourTheme;

public class UINumberSelector extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    private ButtonRegister buttonRegister, internal = new ButtonRegister();
    private UIShapeButton left, right;
    private UITextDisplayBox displayBox;
    private final ButtonOrder buttonOrder;
    private final float x, y, height, displayWidth;
    private final int min, max;
    private int value;
    private UIColourTheme theme = UIColourTheme.GREEN_SELECTED;
    private Runnable onChanged = null;
    private final StaticHitBox hitBox;

    public UINumberSelector(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float height, float displayWidth, int min, int max, int value) {
        super(register, order);
        this.buttonRegister = buttonRegister;
        this.buttonOrder = buttonOrder;
        this.x = x;
        this.y = y;
        this.height = height;
        this.displayWidth = displayWidth;
        this.min = min;
        this.max = max;
        this.value = value;
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
        int prevValue = value;
        value = Math.min(value + 1, max);
        displayBox.setText(String.valueOf(value));
        updateColour();
        if (onChanged != null && prevValue != value)
            onChanged.run();
    }

    private void decrement() {
        int prevValue = value;
        value = Math.max(value - 1, min);
        displayBox.setText(String.valueOf(value));
        updateColour();
        if (onChanged != null && prevValue != value)
            onChanged.run();
    }

    public void setValue(int newValue, boolean triggerOnChanged) {
        int prevValue = value;
        value = Math.clamp(newValue, min, max);
        displayBox.setText(String.valueOf(value));
        updateColour();
        if (triggerOnChanged && onChanged != null && prevValue != value)
            onChanged.run();
    }

    private void updateColour() {
        left.setColourTheme(value == min ? UIColourTheme.GRAYED_OUT : theme);
        right.setColourTheme(value == max ? UIColourTheme.GRAYED_OUT : theme);
    }

    public UINumberSelector setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged;
        return this;
    }

    public int getValue() {
        return value;
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
