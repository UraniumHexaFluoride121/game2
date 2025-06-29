package render.types.text;

import foundation.math.HitBox;
import render.GameRenderer;
import render.HorizontalAlign;
import render.Renderable;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class TextRenderer implements Renderable {
    protected HitBox bounds, boxBounds;
    private final ArrayList<TextRenderElement> elements = new ArrayList<>();
    private float widthNonScaled = 0;
    protected String text;
    protected float textSize;
    private boolean isBold = false, isItalic = false;
    protected HorizontalAlign textAlign = HorizontalAlign.CENTER;
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

    public TextRenderer setTextAlign(HorizontalAlign textAlign) {
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

        if (textAlign == HorizontalAlign.CENTER)
            g.translate(-getTextWidth() / 2 / textSize, 0);
        if (textAlign == HorizontalAlign.RIGHT)
            g.translate(-getTextWidth() / textSize, 0);

        if (renderBoxBorder != -1) {
            boxBounds.fillRounded(g, renderBoxColour, renderBoxRounding * 20);
        }

        GameRenderer.renderTransformed(g, () -> {
            for (TextRenderElement element : elements) {
                applyStyle(element.style, g);
                element.renderable.render(g);
                g.translate(element.width, 0);
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
        return widthNonScaled * textSize;
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
        finalStyle = style;
        while (true) {
            if (separatePosition(current, '[', ']') < separatePosition(current, '{', '}')) {
                String[] styleSeparation = separate(current, '[', ']');
                if (!styleSeparation[0].isEmpty()) {
                    elements.add(new TextRenderElement(new TextLayout(styleSeparation[0], font, g.getFontRenderContext()), style));
                    full.append(styleSeparation[0]);
                }
                if (styleSeparation[1] != null) {
                    style = style.modify(styleSeparation[1]);
                    finalStyle = style;
                    if (styleSeparation[2] == null)
                        break;
                    current = styleSeparation[2];
                } else {
                    break;
                }
            } else {
                String[] renderableSeparation = separate(current, '{', '}');
                if (!renderableSeparation[0].isEmpty()) {
                    elements.add(new TextRenderElement(new TextLayout(renderableSeparation[0], font, g.getFontRenderContext()), style));
                    full.append(renderableSeparation[0]);
                }
                if (renderableSeparation[1] != null) {
                    elements.add(TextRenderable.get(renderableSeparation[1], style));
                    if (renderableSeparation[2] == null)
                        break;
                    current = renderableSeparation[2];
                } else {
                    break;
                }
            }
        }
        widthNonScaled = 0;
        for (TextRenderElement e : elements) {
            widthNonScaled += e.width;
        }
        if (full.isEmpty())
            return;
        TextLayout layout = new TextLayout(full.toString(), font, g.getFontRenderContext());
        Shape textShape = layout.getOutline(null);
        Rectangle2D b = textShape.getBounds2D();
        bounds = HitBox.createFromOriginAndSize((float) b.getX(), (float) b.getY(), (float) b.getWidth(), (float) b.getHeight());
        if (renderBoxBorder != -1) {
            boxBounds = bounds.copy().expand(renderBoxBorder * 20);
        }
    }

    public String getText() {
        if (text == null)
            return "";
        return text;
    }

    private static String[] separate(String s, char startChar, char endChar) {
        int start = s.indexOf(startChar);
        int end = s.indexOf(endChar);
        if (start < end && start != -1) {
            return new String[]{
                    start == 0 ? "" : s.substring(0, start),
                    s.substring(start + 1, end),
                    s.length() - 1 == end ? null : s.substring(end + 1)
            };
        }
        return new String[]{
                s, null, null
        };
    }

    private static int separatePosition(String s, char startChar, char endChar) {
        int start = s.indexOf(startChar);
        int end = s.indexOf(endChar);
        return (start < end && start != -1) ? start : Integer.MAX_VALUE;
    }
}
