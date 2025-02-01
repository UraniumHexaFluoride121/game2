package render.texture;

import foundation.Main;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

@FunctionalInterface
public interface ImageRenderer {
    void render(Graphics2D g, float width);

    static ImageRenderer renderImage(ResourceLocation resource, boolean flip, boolean cacheImage) {
        return renderImage(AssetManager.getImage(resource, cacheImage), flip);
    }

    static ImageRenderer renderImage(BufferedImage image, boolean flip) {
        return (g, blockWidth) -> {
            float s = blockWidth == -1 ? 1 : 1f / image.getWidth() * blockWidth;
            AffineTransform prevTransform = g.getTransform();
            g.scale(s, s);
            if (flip) {
                g.translate(0, image.getHeight());
                g.scale(1, -1);
            }
            g.drawImage(image, 0, 0, Main.window);
            g.setTransform(prevTransform);
        };
    }

    static ImageRenderer renderImageCentered(ResourceLocation resource, boolean flip, boolean cacheImage) {
        return renderImageCentered(AssetManager.getImage(resource, cacheImage), flip);
    }

    static ImageRenderer renderImageCentered(Image image, boolean flip) {
        return (g, blockWidth) -> {
            float s = blockWidth == -1 ? 1 : 1f / image.getWidth(null) * blockWidth;
            AffineTransform prevTransform = g.getTransform();
            g.scale(s, s);
            g.translate(-image.getWidth(null) / 2f, -image.getHeight(null) / 2f);
            if (flip) {
                g.translate(0, image.getHeight(null));
                g.scale(1, -1);
            }
            g.drawImage(image, 0, 0, Main.window);
            g.setTransform(prevTransform);
        };
    }
}
