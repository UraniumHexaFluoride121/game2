package render;

import foundation.Main;
import foundation.MainPanel;
import foundation.math.ObjPos;
import render.texture.AssetManager;
import render.texture.ResourceLocation;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.function.Consumer;

@FunctionalInterface
public interface Renderable {
    int SCALING = 300;

    void render(Graphics2D g);

    default Renderable andThen(Renderable other) {
        return g -> {
            render(g);
            other.render(g);
        };
    }

    default Renderable transform(Consumer<AffineTransform> transformer) {
        return g -> {
            AffineTransform prev = g.getTransform();
            AffineTransform t = new AffineTransform();
            transformer.accept(t);
            g.transform(t);
            render(g);
            g.setTransform(prev);
        };
    }

    default Renderable translate(float x, float y) {
        return g -> {
            AffineTransform prev = g.getTransform();
            g.translate(x, y);
            render(g);
            g.setTransform(prev);
        };
    }

    default Renderable scale(float s) {
        return g -> {
            AffineTransform prev = g.getTransform();
            g.scale(s, s);
            render(g);
            g.setTransform(prev);
        };
    }

    default Renderable scale(float sx, float sy) {
        return g -> {
            AffineTransform prev = g.getTransform();
            g.scale(sx, sy);
            render(g);
            g.setTransform(prev);
        };
    }

    default void preRender() {
        BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dummy.createGraphics();
        render(g);
        g.dispose();
    }

    static BasicStroke roundedStroke(float width) {
        return new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 500);
    }

    static BasicStroke sharpCornerStroke(float width) {
        return new BasicStroke(width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1);
    }

    static Renderable renderImage(ResourceLocation resource, boolean centerImage, boolean flip, float blockWidth, boolean cacheImage) {
        return renderImage(AssetManager.getImage(resource, cacheImage), centerImage, flip, blockWidth);
    }

    static Renderable renderImage(BufferedImage image, boolean centerImage, boolean flip, float blockWidth) {
        Renderable imageRenderable;
        float s = blockWidth == -1 ? 1 : 1f / image.getWidth() * blockWidth;
        if (centerImage) {
            imageRenderable = g -> {
                AffineTransform prevTransform = g.getTransform();

                g.translate(Main.BLOCKS_X / 2f, MainPanel.BLOCK_DIMENSIONS.y / 2f);
                g.scale(s, s);
                g.translate(-image.getWidth() / 2f, -image.getHeight() / 2f);
                if (flip) {
                    g.translate(0, image.getHeight());
                    g.scale(1, -1);
                }
                g.drawImage(image, 0, 0, Main.window);
                g.setTransform(prevTransform);
            };
        } else {
            imageRenderable = g -> {
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
        if (blockWidth == -1)
            imageRenderable = imageRenderable.transform(GameRenderer.renderScaledToBlocks());
        return imageRenderable;
    }

    static Renderable renderImageCentered(ResourceLocation resource, boolean flip, float blockWidth, boolean cacheImage) {
        return renderImageCentered(AssetManager.getImage(resource, cacheImage), flip, blockWidth);
    }

    static Renderable renderImageCentered(BufferedImage image, boolean flip, float blockWidth) {
        float s = blockWidth == -1 ? 1 : 1f / image.getWidth() * blockWidth;
        Renderable imageRenderable = g -> {
            AffineTransform prevTransform = g.getTransform();
            g.scale(s, s);
            g.translate(-image.getWidth() / 2f, -image.getHeight() / 2f);
            if (flip) {
                g.translate(0, image.getHeight());
                g.scale(1, -1);
            }
            g.drawImage(image, 0, 0, Main.window);
            g.setTransform(prevTransform);
        };
        if (blockWidth == -1)
            imageRenderable = imageRenderable.transform(GameRenderer.renderScaledToBlocks());
        return imageRenderable;
    }

    static BufferedImage createImage(float blocksX, float blocksY, int type) {
        return createImage(new ObjPos(blocksX, blocksY), type);
    }

    static BufferedImage createImage(ObjPos blocksSize, int type) {
        ObjPos imageSize = MainPanel.RENDER_WINDOW_SIZE.copy().multiply(blocksSize).divide(MainPanel.BLOCK_DIMENSIONS);
        return new BufferedImage(imageSize.xIntCeil(), imageSize.yIntCeil(), BufferedImage.TYPE_INT_ARGB);
    }

    static BufferedImage renderToImage(BufferedImage image, Renderable renderable) {
        Graphics2D g = image.createGraphics();
        g.scale(MainPanel.windowTransform.getScaleX(), MainPanel.windowTransform.getScaleX());
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        renderable.render(g);
        g.dispose();
        return image;
    }

    static BufferedImage renderToImageNoScale(BufferedImage image, Renderable renderable) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        renderable.render(g);
        g.dispose();
        return image;
    }

    static BufferedImage transparency(BufferedImage image, float alpha) {
        new RescaleOp(new float[]{1, 1, 1, alpha}, new float[]{0, 0, 0, 0}, image.createGraphics().getRenderingHints()).filter(image, image);
        return image;
    }

    static BufferedImage darken(BufferedImage image, float multiplier) {
        new RescaleOp(multiplier, multiplier, image.createGraphics().getRenderingHints()).filter(image, image);
        return image;
    }

    static float top() {
        return MainPanel.BLOCK_DIMENSIONS.y;
    }

    static float right() {
        return MainPanel.BLOCK_DIMENSIONS.x;
    }
}
