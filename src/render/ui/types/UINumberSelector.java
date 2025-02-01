package render.ui.types;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import render.*;
import render.ui.UIColourTheme;

public class UINumberSelector extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    private ButtonRegister buttonRegister, internal = new ButtonRegister();
    private UIShapeButton left, right;
    private UITextDisplayBox displayBox;
    private final ButtonOrder buttonOrder;
    private final float x, y, height, displayWidth;
    private final int min, max;
    private int value;

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
        left = new UIShapeButton(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, x, y, height, height, false)
                .setColourTheme(UIColourTheme.GREEN_SELECTED)
                .setShape(UIShapeButton::triangleLeft)
                .setBoxShape(UIBox.BoxShape.RECTANGLE_LEFT_CORNERS_CUT)
                .setOnClick(this::decrement);
        right = new UIShapeButton(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, x + height + .5f * 2 + displayWidth, y, height, height, false)
                .setColourTheme(UIColourTheme.GREEN_SELECTED)
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
    }

    private void increment() {
        value = Math.min(value + 1, max);
        displayBox.setText(String.valueOf(value));
    }

    private void decrement() {
        value = Math.max(value - 1, min);
        displayBox.setText(String.valueOf(value));
    }

    @Override
    public boolean posInside(ObjPos pos) {
        return enabled;
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
        if (enabled)
            blocking = internal.acceptInput(pos, type, true);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (enabled)
            blocking = internal.acceptInput(pos, type, false);
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
    }
}
