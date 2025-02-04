package render.texture;

import foundation.math.ObjPos;
import render.*;

import java.util.function.Supplier;

public class BackgroundRenderer extends AbstractRenderElement {
    private BackgroundTexture[] textures;

    public BackgroundRenderer(RenderRegister<OrderedRenderable> register, RenderOrder order, Supplier<ObjPos> cameraPosition) {
        super(register, order);
        renderable = g -> {
            for (BackgroundTexture texture : textures) {
                GameRenderer.renderOffset(cameraPosition.get().multiply(texture.cameraMultiplier), g, () -> {
                    texture.renderable.render(g);
                });
            }
        };
    }

    public BackgroundRenderer setTextures(BackgroundTexture[] textures) {
        this.textures = textures;
        return this;
    }
}
