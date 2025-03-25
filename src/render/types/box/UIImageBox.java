package render.types.box;

import render.GameRenderer;
import render.Renderable;
import render.texture.ImageRenderer;

import java.awt.*;

public class UIImageBox extends UIBox {
    private final ImageRenderer image;

    public UIImageBox(float width, float height, ImageRenderer image) {
        super(width, height);
        this.image = image;
    }

    public UIImageBox(float width, float height, float corner, BoxShape shape, ImageRenderer image) {
        super(width, height, corner, shape);
        this.image = image;
    }

    @Override
    public void render(Graphics2D g) {
        super.render(g);
        GameRenderer.renderScaled(1f / SCALING, g, () -> {
            Shape clip = g.getClip();
            g.clip(box);
            GameRenderer.renderOffset(width / 2 * Renderable.SCALING, height / 2 * Renderable.SCALING, g, () -> {
                image.render(g, width * SCALING);
            });
            g.setClip(clip);
        });
    }
}
