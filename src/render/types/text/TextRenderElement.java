package render.types.text;

import render.Renderable;

import java.awt.*;
import java.awt.font.TextLayout;

public class TextRenderElement {
    public final float width;
    public final TextStyle style;
    public final Renderable renderable;

    public TextRenderElement(float width, TextStyle style, Renderable renderable) {
        this.width = width;
        this.style = style;
        this.renderable = renderable;
    }

    public TextRenderElement(TextLayout layout, TextStyle style) {
        width = layout.getAdvance();
        this.style = style;
        Shape shape = layout.getOutline(null);
        renderable = g -> g.fill(shape);
    }

    public TextRenderElement(float advance, float renderWidth, TextStyle style, Renderable renderable) {
        width = advance;
        this.style = style;
        this.renderable = renderable.translate(advance / 2 - renderWidth / 2, 0).scale(1, -1);
    }
}
