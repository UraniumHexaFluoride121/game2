package render.texture;

import foundation.MainPanel;
import foundation.math.ObjPos;
import render.*;

import java.awt.*;
import java.util.function.Supplier;

public class BackgroundRenderer extends AbstractRenderElement {
    private Supplier<ObjPos> cameraPosition;
    private BackgroundTexture[] textures;

    public BackgroundRenderer(RenderRegister<OrderedRenderable> register, RenderOrder order, Supplier<ObjPos> cameraPosition) {
        super(register, order);
        this.cameraPosition = cameraPosition;
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

    @Override
    public void delete() {
        super.delete();
        cameraPosition = null;
    }
}
