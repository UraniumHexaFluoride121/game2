package render.types.box;

import foundation.input.*;
import foundation.math.ObjPos;
import foundation.math.HitBox;
import render.AbstractRenderElement;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.types.text.TooltipManager;

public class UITooltipBox extends AbstractRenderElement implements TooltipHolder, RegisteredButtonInputReceiver {
    private final HitBox hitBox;
    private final ButtonOrder buttonOrder;
    private ButtonRegister buttonRegister;
    private ButtonClickHandler clickHandler;
    private TooltipManager tooltip;

    public UITooltipBox(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, ButtonOrder buttonOrder, float x, float y, float width, float height) {
        super(register, RenderOrder.LEVEL_UI);
        this.buttonOrder = buttonOrder;
        this.buttonRegister = buttonRegister;
        buttonRegister.register(this);
        hitBox = HitBox.createFromOriginAndSize(x, y, width, height);
        clickHandler = new ButtonClickHandler(InputType.MOUSE_LEFT, false);
        tooltip = new TooltipManager(this);
        renderable = tooltip;
    }

    @Override
    public ButtonClickHandler getClickHandler() {
        return clickHandler;
    }

    @Override
    public TooltipManager getManager() {
        return tooltip;
    }

    @Override
    public boolean posInside(ObjPos pos, InputType type) {
        return hitBox.isPositionInside(pos);
    }

    @Override
    public boolean blocking(InputType type) {
        return false;
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return buttonOrder;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        clickHandler.buttonPressed(pos, inside, blocked, type);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        clickHandler.buttonPressed(pos, inside, blocked, type);
    }

    @Override
    public void delete() {
        super.delete();
        buttonRegister.remove(this);
        buttonRegister = null;
    }
}
