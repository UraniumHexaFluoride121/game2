package render.renderables;

import render.*;

import java.awt.geom.AffineTransform;
import java.util.function.Consumer;

public class RenderElement extends AbstractRenderElement {
    public RenderElement(RenderRegister<OrderedRenderable> register, RenderOrder order, Renderable... renderables) {
        super(register, order);
        renderable = g -> {
            if (!enabled)
                return;
            for (Renderable r : renderables) {
                r.render(g);
            }
        };
    }

    @Override
    public RenderElement transform(Consumer<AffineTransform> transformer) {
        renderable = renderable.transform(transformer);
        return this;
    }

    @Override
    public RenderElement translate(float x, float y) {
        renderable = renderable.translate(x, y);
        return this;
    }

    @Override
    public RenderElement scale(float s) {
        renderable = renderable.scale(s);
        return this;
    }

    @Override
    public RenderElement setZOrder(int zOrder) {
        super.setZOrder(zOrder);
        return this;
    }
}
