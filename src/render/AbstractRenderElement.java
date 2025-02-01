package render;

import foundation.Deletable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.function.Consumer;

public abstract class AbstractRenderElement implements OrderedRenderable, Deletable {
    protected RenderRegister<OrderedRenderable> register;
    protected final RenderOrder order;
    protected Renderable renderable;

    public boolean enabled = true;

    protected int zOrder = 0;

    public AbstractRenderElement(RenderRegister<OrderedRenderable> register, RenderOrder order) {
        this.register = register;
        this.order = order;
        if (register != null)
            register.register(this);
    }

    public AbstractRenderElement setZOrder(int zOrder) {
        this.zOrder = zOrder;
        return this;
    }

    public AbstractRenderElement setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public void delete() {
        if (register != null) {
            register.remove(this);
            register = null;
        }
        renderable = null;
    }

    @Override
    public RenderOrder getRenderOrder() {
        return order;
    }

    @Override
    public int getZOrder() {
        return zOrder;
    }

    @Override
    public AbstractRenderElement transform(Consumer<AffineTransform> transformer) {
        renderable = renderable.transform(transformer);
        return this;
    }

    @Override
    public AbstractRenderElement translate(float x, float y) {
        renderable = renderable.translate(x, y);
        return this;
    }

    @Override
    public AbstractRenderElement scale(float s) {
        renderable = renderable.scale(s);
        return this;
    }

    @Override
    public AbstractRenderElement scale(float sx, float sy) {
        renderable = renderable.scale(sx, sy);
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        renderable.render(g);
    }
}
