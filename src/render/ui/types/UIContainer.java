package render.ui.types;

import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import render.*;

import java.awt.geom.AffineTransform;
import java.util.function.BiConsumer;

public class UIContainer extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    public final float x, y;
    protected final ButtonOrder buttonOrder;
    protected ButtonRegister buttonRegister, internal = new ButtonRegister();
    protected GameRenderer renderer = new GameRenderer(new AffineTransform(), null);

    public UIContainer(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, BiConsumer<GameRenderer, ButtonRegister> createRenderer) {
        this(register, buttonRegister, order, buttonOrder, x, y);
        addRenderables(createRenderer);
    }

    public UIContainer(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y) {
        super(register, order);
        this.x = x;
        this.y = y;
        this.buttonOrder = buttonOrder;
        this.buttonRegister = buttonRegister;
        if (buttonRegister != null) {
            buttonRegister.register(this);
        }
        renderable = g -> {
            if (!enabled)
                return;
            GameRenderer.renderOffset(x, y, g, () -> {
                renderer.render(g);
            });
        };
    }

    public UIContainer addRenderables(BiConsumer<GameRenderer, ButtonRegister> createRenderer) {
        createRenderer.accept(renderer, internal);
        return this;
    }

    public UIContainer setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
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
            blocking = internal.acceptInput(pos.copy().subtract(x, y), type, true);
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (enabled)
            blocking = internal.acceptInput(pos.copy().subtract(x, y), type, false);
    }

    @Override
    public void delete() {
        super.delete();
        internal.delete();
        renderer.delete();
        if (buttonRegister != null) {
            buttonRegister.remove(this);
            buttonRegister = null;
        }
    }
}
