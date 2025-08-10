package render.types.box;

import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import foundation.math.HitBox;
import render.OrderedRenderable;
import render.RenderOrder;
import render.RenderRegister;
import render.level.tile.RenderElement;

import java.util.function.UnaryOperator;

public class UIClickBlockingBox extends RenderElement implements RegisteredButtonInputReceiver {
    private final HitBox hitBox;
    private UnaryOperator<ObjPos> posTransformer = p -> p;
    private ButtonRegister buttonRegister;

    public UIClickBlockingBox(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, float x, float y, float width, float height) {
        super(register, order, new UIBox(width, height).translate(x, y));
        this.buttonRegister = buttonRegister;
        if (buttonRegister != null) {
            buttonRegister.register(this);
        }
        hitBox = HitBox.createFromOriginAndSize(x, y, width, height);
    }

    public UIClickBlockingBox(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, float x, float y, float width, float height, UnaryOperator<UIBox> boxTransformer) {
        super(register, order, boxTransformer.apply(new UIBox(width, height)).translate(x, y));
        this.buttonRegister = buttonRegister;
        if (buttonRegister != null) {
            buttonRegister.register(this);
        }
        hitBox = HitBox.createFromOriginAndSize(x, y, width, height);
    }

    public UIClickBlockingBox setPosTransformer(UnaryOperator<ObjPos> posTransformer) {
        this.posTransformer = posTransformer;
        return this;
    }

    @Override
    public boolean posInside(ObjPos pos, InputType type) {
        return isEnabled() && hitBox.isPositionInside(posTransformer.apply(pos));
    }

    @Override
    public boolean blocking(InputType type) {
        return type.isMouseInput();
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {

    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {

    }

    @Override
    public void delete() {
        super.delete();
        if (buttonRegister != null) {
            buttonRegister.remove(this);
            buttonRegister = null;
        }
        posTransformer = null;
    }
}
