package render.types.input.button;

import foundation.input.*;
import foundation.math.ObjPos;
import foundation.math.StaticHitBox;
import render.AbstractRenderElement;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.UIColourTheme;
import render.types.box.UIBox;
import render.types.text.TooltipManager;

import java.util.function.Consumer;

public abstract class AbstractUIButton extends AbstractRenderElement implements RegisteredButtonInputReceiver, TooltipHolder {
    public final float x, y, height, width;
    protected final ButtonClickHandler clickHandler;
    protected final UIBox box;
    protected final StaticHitBox hitBox;
    protected final ButtonOrder buttonOrder;
    protected ButtonRegister buttonRegister;
    protected TooltipManager tooltip = new TooltipManager(this);

    protected boolean clickEnabled = true;

    public AbstractUIButton(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float height, float width, boolean staySelected, Runnable onClick) {
        super(register, order);
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.buttonOrder = buttonOrder;
        this.buttonRegister = buttonRegister;
        if (buttonRegister != null) {
            buttonRegister.register(this);
        }
        clickHandler = new ButtonClickHandler(InputType.MOUSE_LEFT, staySelected, onClick);
        box = new UIBox(width, height).setClickHandler(clickHandler);
        hitBox = StaticHitBox.createFromOriginAndSize(x, y, width, height);
    }

    @Override
    public TooltipManager getManager() {
        return tooltip;
    }

    public AbstractUIButton noDeselect() {
        clickHandler.noDeselect();
        return this;
    }

    public AbstractUIButton toggleMode() {
        clickHandler.toggleMode();
        return this;
    }

    public AbstractUIButton deselect() {
        clickHandler.deselect();
        return this;
    }

    public AbstractUIButton setColourTheme(UIColourTheme colourTheme) {
        box.setColourTheme(colourTheme);
        return this;
    }

    public AbstractUIButton setOnDeselect(Runnable runnable) {
        clickHandler.setOnDeselect(runnable);
        return this;
    }

    public AbstractUIButton setOnClick(Runnable runnable) {
        clickHandler.setOnClick(runnable);
        return this;
    }

    public AbstractUIButton setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    public AbstractUIButton setClickEnabled(boolean clickEnabled) {
        this.clickEnabled = clickEnabled;
        return this;
    }

    public AbstractUIButton select() {
        clickHandler.select();
        return this;
    }

    public AbstractUIButton runOnCLick() {
        clickHandler.runOnClick();
        return this;
    }

    public AbstractUIButton setBoxShape(UIBox.BoxShape shape) {
        box.setShape(shape);
        return this;
    }

    public AbstractUIButton setBoxCorner(float corner) {
        box.setCorner(corner);
        return this;
    }

    @Override
    public ButtonClickHandler getClickHandler() {
        return clickHandler;
    }

    public boolean isSelected() {
        return clickHandler.isSelected();
    }

    @Override
    public boolean posInside(ObjPos pos, InputType type) {
        return isEnabled() && hitBox.isPositionInside(pos);
    }

    @Override
    public boolean blocking(InputType type) {
        return type.isMouseInput();
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return buttonOrder;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (isEnabled() && clickEnabled)
            clickHandler.buttonPressed(pos, inside, blocked, type);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (isEnabled() && clickEnabled)
            clickHandler.buttonReleased(pos, inside, blocked, type);
    }

    @Override
    public void delete() {
        super.delete();
        clickHandler.delete();
        box.setClickHandler(null);
        if (buttonRegister != null) {
            buttonRegister.remove(this);
            buttonRegister = null;
        }
        tooltip.delete();
    }
}
