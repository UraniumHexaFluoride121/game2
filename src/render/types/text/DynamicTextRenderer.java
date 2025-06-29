package render.types.text;

import foundation.Deletable;
import foundation.math.HitBox;
import render.HorizontalAlign;
import render.Renderable;

import java.awt.*;
import java.awt.font.TextLayout;
import java.util.function.Supplier;

public class DynamicTextRenderer implements Renderable, Deletable {
    private HitBox bounds, boxBounds;
    private String cachedText;
    private Shape textShape;
    private Supplier<String> text;
    private final float textSize;
    private boolean isBold = false;
    private boolean hasOutline = false;
    private HorizontalAlign textAlign = HorizontalAlign.CENTER;
    private final Color main;
    private Color border, renderBoxColour;
    private BasicStroke stroke;
    private float renderBoxBorder = -1, renderBoxRounding;

    public DynamicTextRenderer(Supplier<String> text, float textSize, Color main) {
        this.text = text;
        this.textSize = textSize / 20;
        this.main = main;
    }

    public DynamicTextRenderer setBold(boolean isBold) {
        this.isBold = isBold;
        return this;
    }

    public DynamicTextRenderer setOutline(Color borderColour, float outlineSize) {
        hasOutline = true;
        border = borderColour;
        stroke = new BasicStroke(outlineSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 100);
        return this;
    }

    public DynamicTextRenderer setRenderBorder(float border, float rounding, Color colour) {
        renderBoxBorder = border;
        renderBoxRounding = rounding;
        renderBoxColour = colour;
        update();
        return this;
    }

    public DynamicTextRenderer setTextAlign(HorizontalAlign textAlign) {
        this.textAlign = textAlign;
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        if (cachedText == null || !cachedText.equals(text.get()))
            updateText(g);
        g.scale(textSize, -textSize);

        if (textAlign != HorizontalAlign.LEFT) {
            if (textAlign == HorizontalAlign.CENTER)
                g.translate(-bounds.width() / 2f, 0);
            if (textAlign == HorizontalAlign.RIGHT)
                g.translate(-bounds.width(), 0);
        }

        if (hasOutline) {
            g.setStroke(stroke);
            g.setColor(border);
            g.draw(textShape);
        }

        if (renderBoxBorder != -1) {
            boxBounds.fillRounded(g, renderBoxColour, renderBoxRounding * 20);
        }

        g.setColor(main);
        g.fill(textShape);
        g.scale(1 / textSize, -1 / textSize);
    }

    public void update() {
        cachedText = null;
    }

    private void updateText(Graphics2D g) {
        cachedText = text.get();
        Font font = new Font(null, isBold ? Font.BOLD : Font.PLAIN, 20);
        textShape = new TextLayout(text.get(), font, g.getFontRenderContext()).getOutline(null);
        Rectangle b = textShape.getBounds();
        bounds = HitBox.createFromOriginAndSize(b.x, b.y, b.width, b.height);
        if (renderBoxBorder != -1) {
            boxBounds = bounds.copy().expand(renderBoxBorder * 20);
        }
    }

    @Override
    public void delete() {
        text = null;
    }
}
