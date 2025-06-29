package render.types.input;

import foundation.input.*;
import foundation.math.ObjPos;
import foundation.math.HitBox;
import render.*;
import render.types.box.UIBox;
import render.types.input.button.UIShapeButton;
import render.types.text.TooltipManager;
import render.types.text.UITextDisplayBox;

public abstract class AbstractValueSelector<T> extends AbstractRenderElement implements RegisteredButtonInputReceiver, TooltipHolder {
    protected ButtonRegister buttonRegister;
    protected final ButtonRegister internal = new ButtonRegister();
    protected final UIShapeButton left, right;
    protected final UITextDisplayBox displayBox;
    protected final ButtonOrder buttonOrder;
    protected final float height, displayWidth;
    protected T value;
    protected int index;
    protected UIColourTheme theme = UIColourTheme.GREEN_SELECTED, displayTheme = UIColourTheme.GREEN_SELECTED;
    protected Runnable onChanged = null;
    protected final HitBox hitBox;
    protected ButtonClickHandler tooltipClickHandler = new ButtonClickHandler(InputType.MOUSE_LEFT, false);
    protected final TooltipManager tooltip = new TooltipManager(this);

    public AbstractValueSelector(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float height, float displayWidth) {
        super(register, order);
        this.buttonRegister = buttonRegister;
        this.buttonOrder = buttonOrder;
        this.height = height;
        this.displayWidth = displayWidth;
        hitBox = HitBox.createFromOriginAndSize(x, y, 2 * height + .5f * 2 + displayWidth, height);
        left = new UIShapeButton(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, x, y, height, height, false)
                .setColourTheme(theme)
                .setShape(UIShapeButton::triangleLeft)
                .setBoxShape(UIBox.BoxShape.RECTANGLE_LEFT_CORNERS_CUT)
                .setBoxCorner(0.5f)
                .setOnClick(this::decrement);
        right = new UIShapeButton(null, internal, RenderOrder.NONE, ButtonOrder.MAIN_BUTTONS, x + height + .5f * 2 + displayWidth, y, height, height, false)
                .setColourTheme(theme)
                .setShape(UIShapeButton::triangleRight)
                .setBoxShape(UIBox.BoxShape.RECTANGLE_RIGHT_CORNERS_CUT)
                .setBoxCorner(0.5f)
                .setOnClick(this::increment);
        displayBox = new UITextDisplayBox(null, RenderOrder.NONE, x + height + .5f, y, displayWidth, height, height * 0.8f)
                .setBold().setBoxShape(UIBox.BoxShape.RECTANGLE).setColourTheme(displayTheme);
        renderable = g -> {
            left.render(g);
            right.render(g);
            displayBox.render(g);
            tooltip.render(g);
        };
        if (buttonRegister != null)
            buttonRegister.register(this);
    }

    protected void init(int initialIndex) {
        index = initialIndex;
        value = getItem(index);
        displayBox.setText(getName(value));
        updateColour();
    }

    public float totalWidth() {
        return 2 * height + .5f * 2 + displayWidth;
    }

    public static float totalWidth(float height, float displayWidth) {
        return 2 * height + .5f * 2 + displayWidth;
    }

    protected abstract T getItem(int index);

    protected abstract int indexOf(T item);

    protected abstract String getName(T item);

    protected abstract int maxIndex();

    public AbstractValueSelector<T> setValue(T value, boolean triggerOnChanged) {
        if (this.value != value) {
            updateIndex(indexOf(value), triggerOnChanged);
        }
        return this;
    }

    private void increment() {
        updateIndex(Math.min(index + 1, maxIndex()), true);
    }

    private void decrement() {
        updateIndex(Math.max(index - 1, 0), true);
    }

    private void updateIndex(int index, boolean runOnChanged) {
        this.index = index;
        T prevValue = value;
        value = getItem(index);
        displayBox.setText(getName(value));
        updateColour();
        if (runOnChanged && onChanged != null && prevValue != value)
            onChanged.run();
    }

    private void updateColour() {
        left.setColourTheme(index == 0 ? UIColourTheme.GRAYED_OUT : theme);
        right.setColourTheme(index == maxIndex() ? UIColourTheme.GRAYED_OUT : theme);
        displayBox.setColourTheme(displayTheme);
    }

    public AbstractValueSelector<T> setDisplayTheme(UIColourTheme displayTheme) {
        this.displayTheme = displayTheme;
        updateColour();
        return this;
    }

    public AbstractValueSelector<T> setButtonTheme(UIColourTheme theme) {
        this.theme = theme;
        updateColour();
        return this;
    }

    public T getValue() {
        return value;
    }

    public AbstractValueSelector<T> setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged;
        return this;
    }

    public AbstractValueSelector<T> setCorner(float corner) {
        left.setBoxCorner(corner);
        right.setBoxCorner(corner);
        return this;
    }

    @Override
    public ButtonClickHandler getClickHandler() {
        return tooltipClickHandler;
    }

    @Override
    public TooltipManager getManager() {
        return tooltip;
    }

    @Override
    public boolean posInside(ObjPos pos, InputType type) {
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
            tooltipClickHandler.buttonPressed(pos, inside, blocked, type);
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
        tooltip.delete();
    }
}
