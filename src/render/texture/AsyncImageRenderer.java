package render.texture;

import java.awt.*;

public class AsyncImageRenderer implements ImageRenderer {
    private ImageRenderer renderer = null;

    public AsyncImageRenderer() {
    }

    public synchronized void setRenderer(ImageRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public synchronized void render(Graphics2D g, float width) {
        if (renderer == null)
            return;
        renderer.render(g, width);
    }
}
