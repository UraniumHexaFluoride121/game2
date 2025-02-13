package render.ui.types;

import render.*;
import render.ui.UIColourTheme;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.function.Function;

import static level.tile.Tile.*;

public class UIShapeDisplayBox extends AbstractRenderElement {
    private Shape renderShape = null;
    public final float x, y, height, width;
    protected final UIBox box;

    public UIShapeDisplayBox(RenderRegister<OrderedRenderable> register, RenderOrder order, float x, float y, float width, float height) {
        super(register, order);
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        box = new UIBox(width, height);
        renderable = g -> {
            if (!isEnabled())
                return;
            GameRenderer.renderOffset(x, y, g, () -> {
                box.render(g);
                g.setColor(UITextLabel.TEXT_COLOUR);
                if (renderShape != null) {
                    GameRenderer.renderScaled(1f / SCALING, g, () -> {
                        g.fill(renderShape);
                    });
                }
            });
        };
    }

    public UIShapeDisplayBox drawShape(float width) {
        BasicStroke stroke = Renderable.roundedStroke(width * SCALING);
        renderable = g -> {
            if (!isEnabled())
                return;
            GameRenderer.renderOffset(x, y, g, () -> {
                box.render(g);
                g.setColor(UITextLabel.TEXT_COLOUR);
                if (renderShape != null) {
                    GameRenderer.renderScaled(1f / SCALING, g, () -> {
                        g.setStroke(stroke);
                        g.draw(renderShape);
                    });
                }
            });
        };
        return this;
    }

    public UIShapeDisplayBox setColourTheme(UIColourTheme colourTheme) {
        box.setColourTheme(colourTheme);
        return this;
    }

    public UIShapeDisplayBox setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        return this;
    }

    public UIShapeDisplayBox setShape(Function<UIBox, Shape> shapeGenerator) {
        renderShape = shapeGenerator.apply(box);
        return this;
    }

    public UIShapeDisplayBox setBoxShape(UIBox.BoxShape shape) {
        box.setShape(shape);
        return this;
    }

    public UIShapeDisplayBox setBoxCorner(float corner) {
        box.setCorner(corner);
        return this;
    }
}
