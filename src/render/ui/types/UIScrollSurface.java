package render.ui.types;

import foundation.MainPanel;
import foundation.input.ButtonOrder;
import foundation.input.ButtonRegister;
import foundation.input.InputType;
import foundation.input.RegisteredButtonInputReceiver;
import foundation.math.ObjPos;
import render.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.function.BiConsumer;

public class UIScrollSurface extends AbstractRenderElement implements RegisteredButtonInputReceiver {
    private final float x, y, width, height;
    private final Rectangle2D clip;
    private ButtonOrder buttonOrder;
    private float scrollAmount = 0, scrollMin = 0, scrollMax = 0, scrollSpeed = 1;
    private ButtonRegister buttonRegister, internal = new ButtonRegister();
    private GameRenderer renderer = new GameRenderer(new AffineTransform(), null);

    public UIScrollSurface(RenderRegister<OrderedRenderable> register, ButtonRegister buttonRegister, RenderOrder order, ButtonOrder buttonOrder, float x, float y, float width, float height, BiConsumer<GameRenderer, ButtonRegister> elementsRenderer) {
        super(register, order);
        this.buttonRegister = buttonRegister;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.buttonOrder = buttonOrder;
        if (buttonRegister != null)
            buttonRegister.register(this);
        clip = new Rectangle2D.Float(0, 0, width, height);
        renderable = g -> {
            Shape prevClip = g.getClip();
            GameRenderer.renderOffset(x, y, g, () -> {
                g.setClip(clip);
                g.scale(1, -1);
                g.translate(0, -scrollAmount - height);
                renderer.render(g);
            });
            g.setClip(prevClip);
        };
        elementsRenderer.accept(renderer, internal);
    }

    public UIScrollSurface setScrollMax(float scrollMax) {
        this.scrollMax = Math.max(scrollMin, scrollMax);
        scrollAmount = Math.clamp(scrollAmount, scrollMin, this.scrollMax);
        return this;
    }

    @Override
    public boolean posInside(ObjPos pos) {
        return clip.contains(pos.x - x, pos.y - y);
    }

    private boolean blocking = false;

    @Override
    public boolean blocking(InputType type) {
        return true;
    }

    @Override
    public ButtonOrder getButtonOrder() {
        return buttonOrder;
    }

    @Override
    public void buttonPressed(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!enabled)
            return;
        if (inside)
            blocking = internal.acceptInput(pos.copy().subtract(x, y).multiply(1, -1).add(0, scrollAmount + height), type, true);
        else
            blocking = internal.acceptInput(new ObjPos(-1000, -1000), type, true);
        if (!blocking && inside && !blocked && type.isScrollInput()) {
            if (type == InputType.MOUSE_SCROLL_UP)
                scrollAmount = Math.clamp(scrollAmount - scrollSpeed, scrollMin, scrollMax);
            else
                scrollAmount = Math.clamp(scrollAmount + scrollSpeed, scrollMin, scrollMax);
        }
    }

    @Override
    public void buttonReleased(ObjPos pos, boolean inside, boolean blocked, InputType type) {
        if (!enabled)
            return;
        if (inside)
            blocking = internal.acceptInput(pos.copy().subtract(x, y).multiply(1, -1).add(0, scrollAmount + height), type, false);
        else
            blocking = internal.acceptInput(new ObjPos(-1000, -1000), type, false);
    }

    @Override
    public void delete() {
        super.delete();
        if (buttonRegister != null) {
            buttonRegister.remove(this);
            buttonRegister = null;
        }
        internal.delete();
        renderer.delete();
    }
}
