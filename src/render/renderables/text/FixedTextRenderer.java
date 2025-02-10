package render.renderables.text;

import foundation.math.StaticHitBox;
import render.Renderable;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

public class FixedTextRenderer implements Renderable {
    protected StaticHitBox bounds, boxBounds;
    private Shape textShape;
    protected String text;
    protected float textSize;
    private boolean isBold = false;
    private boolean hasOutline = false;
    protected TextAlign textAlign = TextAlign.CENTER;
    private final Color main;
    private Color border, renderBoxColour;
    private BasicStroke stroke;
    private float renderBoxBorder = -1, renderBoxRounding;

    public FixedTextRenderer(String text, float textSize, Color main) {
        this.text = text;
        this.textSize = textSize / 20;
        this.main = main;
        update();
    }

    public FixedTextRenderer setBold(boolean isBold) {
        this.isBold = isBold;
        update();
        return this;
    }

    public FixedTextRenderer setTextSize(float textSize) {
        this.textSize = textSize / 20;
        return this;
    }

    public FixedTextRenderer setOutline(Color borderColour, float outlineSize) {
        hasOutline = true;
        border = borderColour;
        stroke = new BasicStroke(outlineSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 100);
        update();
        return this;
    }

    public FixedTextRenderer setRenderBorder(float border, float rounding, Color colour) {
        renderBoxBorder = border;
        renderBoxRounding = rounding;
        renderBoxColour = colour;
        update();
        return this;
    }

    public FixedTextRenderer setTextAlign(TextAlign textAlign) {
        this.textAlign = textAlign;
        update();
        return this;
    }

    private boolean update = false;

    @Override
    public void render(Graphics2D g) {
        if (update) {
            update = false;
            updateText(g);
        }
        if (text == null)
            return;
        g.scale(textSize, -textSize);

        if (textAlign == TextAlign.CENTER)
            g.translate(-bounds.middleX(), 0);
        if (textAlign == TextAlign.RIGHT)
            g.translate(-bounds.getRight(), 0);

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
        update = true;
    }

    public void updateText(String text) {
        if (this.text == null || !this.text.equals(text)) {
            this.text = text;
            update = true;
        }
    }

    protected Font font;

    protected void updateText(Graphics2D g) {
        if (text == null)
            return;
        font = new Font(null, isBold ? Font.BOLD : Font.PLAIN, 20);
        textShape = new TextLayout(text, font, g.getFontRenderContext()).getOutline(null);
        Rectangle2D b = textShape.getBounds2D();
        bounds = StaticHitBox.createFromOriginAndSize((float) b.getX(), (float) b.getY(), (float) b.getWidth(), (float) b.getHeight());
        if (renderBoxBorder != -1) {
            boxBounds = bounds.copy().expand(renderBoxBorder * 20);
        }
    }

    public String getText() {
        return text;
    }
}
