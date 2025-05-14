package render.types.text;

import foundation.math.StaticHitBox;
import render.GameRenderer;
import render.Renderable;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class TextRenderer implements Renderable {
    protected StaticHitBox bounds, boxBounds;
    private Shape textShape;
    private TextLayout layout;
    private final ArrayList<TextRenderElement> elements = new ArrayList<>();
    protected String text;
    protected float textSize;
    private boolean isBold = false, isItalic = false;
    protected TextAlign textAlign = TextAlign.CENTER;
    private Color main, renderBoxColour;
    private float renderBoxBorder = -1, renderBoxRounding;
    private TextStyle initialStyle = new TextStyle(), finalStyle = null;

    public TextRenderer(String text, float textSize, Color main) {
        this.text = text;
        this.textSize = textSize / 20;
        this.main = main;
        update();
    }

    public TextRenderer setTextColour(Color colour) {
        main = colour;
        return this;
    }

    public TextRenderer setInitialStyle(TextStyle initialStyle) {
        this.initialStyle = initialStyle;
        update();
        return this;
    }

    public TextStyle getFinalStyle() {
        return finalStyle;
    }

    public TextRenderer setBold(boolean isBold) {
        this.isBold = isBold;
        isItalic = false;
        update();
        return this;
    }

    public TextRenderer setItalic(boolean isItalic) {
        this.isItalic = isItalic;
        isBold = false;
        update();
        return this;
    }

    public TextRenderer setTextSize(float textSize) {
        this.textSize = textSize / 20;
        return this;
    }

    public TextRenderer setRenderBorder(float border, float rounding, Color colour) {
        renderBoxBorder = border;
        renderBoxRounding = rounding;
        renderBoxColour = colour;
        update();
        return this;
    }

    public TextRenderer setTextAlign(TextAlign textAlign) {
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
            g.translate(-getTextWidth() / 2 / textSize, 0);
        if (textAlign == TextAlign.RIGHT)
            g.translate(-getTextWidth() / textSize, 0);

        if (renderBoxBorder != -1) {
            boxBounds.fillRounded(g, renderBoxColour, renderBoxRounding * 20);
        }

        GameRenderer.renderTransformed(g, () -> {
            for (TextRenderElement element : elements) {
                applyStyle(element.style, g);
                g.fill(element.textShape);
                g.translate(getTextWidthNonScaled(element.layout), 0);
            }
        });
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

    private void applyStyle(TextStyle style, Graphics2D g) {
        g.setColor(style.colour == null ? main : style.colour);
    }

    public float getTextWidth() {
        return getTextWidthNonScaled(layout) * textSize;
    }

    public float getTextWidthNonScaled(TextLayout layout) {
        return layout.getAdvance();
    }

    protected Font font;

    protected void updateText(Graphics2D g) {
        if (text == null)
            return;
        font = new Font(null, isBold ? Font.BOLD : isItalic ? Font.ITALIC : Font.PLAIN, 20);
        elements.clear();
        String current = text;
        StringBuilder full = new StringBuilder();
        TextStyle style = initialStyle;
        while (true) {
            int start = current.indexOf('[');
            int end = current.indexOf(']');
            if (start < end && start != -1) {
                if (!elements.isEmpty() || start != 0) {
                    String s = current.substring(0, start);
                    if (!s.isEmpty()) {
                        elements.add(new TextRenderElement(new TextLayout(s, font, g.getFontRenderContext()), style));
                        full.append(s);
                    }
                }
                style = style.modify(current.substring(start + 1, end));
                finalStyle = style;
                if (current.length() - 1 == end) {
                    break;
                }
                current = current.substring(end + 1);
            } else {
                elements.add(new TextRenderElement(new TextLayout(current, font, g.getFontRenderContext()), style));
                full.append(current);
                finalStyle = elements.getLast().style;
                break;
            }
        }
        layout = new TextLayout(full.toString(), font, g.getFontRenderContext());
        textShape = layout.getOutline(null);
        Rectangle2D b = textShape.getBounds2D();
        bounds = StaticHitBox.createFromOriginAndSize((float) b.getX(), (float) b.getY(), (float) b.getWidth(), (float) b.getHeight());
        if (renderBoxBorder != -1) {
            boxBounds = bounds.copy().expand(renderBoxBorder * 20);
        }
    }

    public String getText() {
        if (text == null)
            return "";
        return text;
    }

    public static class TextRenderElement {
        public final TextLayout layout;
        public final TextStyle style;
        public final Shape textShape;

        public TextRenderElement(TextLayout layout, TextStyle style) {
            this.layout = layout;
            this.style = style;
            textShape = layout.getOutline(null);
        }
    }
}
