package render.ui.types;

import render.GameRenderer;
import render.Renderable;
import render.renderables.text.FixedTextRenderer;
import render.renderables.text.TextAlign;
import render.ui.UIColourTheme;

import java.awt.*;

public class UITextLabel implements Renderable {
    public static final Color TEXT_COLOUR = new Color(181, 204, 216);
    public static final Color GREEN_TEXT_COLOUR = new Color(105, 214, 87);
    public static final Color RED_TEXT_COLOUR = new Color(214, 83, 83);
    public static final Color TEXT_COLOUR_DARK = new Color(153, 188, 200);
    public static final float DEFAULT_LINE_WIDTH = 0.17f;

    private Polygon label;
    private final Polygon line;
    private final FixedTextRenderer textRight, textLeft, textCenter;
    private final float height, lineWidth;
    private float width, rightOffset = 0, leftOffset = 0;
    private final boolean hasLines;
    private Color lineColour = UIColourTheme.LIGHT_BLUE.borderColour, backgroundColour = UIColourTheme.LIGHT_BLUE.backgroundColour;

    public UITextLabel(float width, float height, boolean hasLines) {
        this(width, height, hasLines, DEFAULT_LINE_WIDTH);
    }

    public UITextLabel(float width, float height, boolean hasLines, float lineWidth) {
        this(width, height, hasLines, lineWidth, 0.9f);
    }

    public UITextLabel(float width, float height, boolean hasLines, float lineWidth, float textSizeFactor) {
        this.width = width;
        this.height = height;
        this.lineWidth = lineWidth;
        this.hasLines = hasLines;
        line = line(lineWidth, height * 1.1f, height * 0.1f, (float) Math.toRadians(60));
        label = line(width, height, (float) Math.toRadians(60));
        textRight = new FixedTextRenderer(null, height * textSizeFactor, TEXT_COLOUR).setTextAlign(TextAlign.RIGHT);
        textLeft = new FixedTextRenderer(null, height * textSizeFactor, TEXT_COLOUR).setTextAlign(TextAlign.LEFT);
        textCenter = new FixedTextRenderer(null, height * textSizeFactor, TEXT_COLOUR).setTextAlign(TextAlign.CENTER);
    }

    @Override
    public void render(Graphics2D g) {
        GameRenderer.renderScaled(1f / SCALING, g, () -> {
            g.setColor(backgroundColour);
            g.fill(label);
            if (hasLines) {
                g.translate(lineWidth * SCALING, 0);
                g.setColor(lineColour);
                g.fill(line);
                g.translate(lineWidth * 2 * SCALING, 0);
                g.fill(line);
                g.translate(lineWidth * 2 * SCALING, 0);
            }
        });
        GameRenderer.renderOffset(0.5f * height + leftOffset, .1f * height, g, () -> {
            if (hasLines)
                g.translate(lineWidth * 4, 0);
            textLeft.render(g);
        });
        GameRenderer.renderOffset(width - 0.1f * height - rightOffset, .1f * height, g, () -> {
            textRight.render(g);
        });
        GameRenderer.renderOffset((0.5f * height + leftOffset + width - 0.1f * height - rightOffset) / 2, .1f * height, g, () -> {
            textCenter.render(g);
        });
    }

    public UITextLabel updateTextRight(String s) {
        textRight.updateText(s);
        return this;
    }

    public UITextLabel updateTextLeft(String s) {
        textLeft.updateText(s);
        return this;
    }

    public UITextLabel updateTextCenter(String s) {
        textCenter.updateText(s);
        return this;
    }

    public UITextLabel updateLabelWidth(float width) {
        this.width = width;
        label = line(width, height, (float) Math.toRadians(60));
        return this;
    }

    public UITextLabel setLabelColour(UIColourTheme labelColour) {
        backgroundColour = labelColour.backgroundColour;
        lineColour = labelColour.borderColour;
        return this;
    }

    public UITextLabel setLineColour(Color lineColour) {
        this.lineColour = lineColour;
        return this;
    }

    public UITextLabel setBackgroundColour(Color backgroundColour) {
        this.backgroundColour = backgroundColour;
        return this;
    }

    public UITextLabel setRightColour(Color colour) {
        textRight.setTextColour(colour);
        return this;
    }

    public UITextLabel setCenterColour(Color colour) {
        textCenter.setTextColour(colour);
        return this;
    }

    public UITextLabel setLeftColour(Color colour) {
        textLeft.setTextColour(colour);
        return this;
    }

    private static Polygon line(float width, float height, float angle) {
        return new Polygon(
                new int[]{
                        0,
                        (int) (height * Math.cos(angle) * SCALING),
                        (int) ((height * Math.cos(angle) + width) * SCALING),
                        (int) (width * SCALING)
                },
                new int[]{
                        0,
                        (int) (height * Math.sin(angle) * SCALING),
                        (int) (height * Math.sin(angle) * SCALING),
                        0
                },
                4
        );
    }

    private static Polygon line(float width, float height, float down, float angle) {
        return new Polygon(
                new int[]{
                        (int) (-down * Math.cos(angle) * SCALING),
                        (int) (height * Math.cos(angle) * SCALING),
                        (int) ((height * Math.cos(angle) + width) * SCALING),
                        (int) ((-down * Math.cos(angle) + width) * SCALING),
                },
                new int[]{
                        (int) (-down * Math.sin(angle) * SCALING),
                        (int) (height * Math.sin(angle) * SCALING),
                        (int) (height * Math.sin(angle) * SCALING),
                        (int) (-down * Math.sin(angle) * SCALING)
                },
                4
        );
    }

    public UITextLabel setRightOffset(float rightOffset) {
        this.rightOffset = rightOffset;
        return this;
    }

    public UITextLabel setLeftOffset(float leftOffset) {
        this.leftOffset = leftOffset;
        return this;
    }

    public UITextLabel setTextRightBold() {
        textRight.setBold(true);
        return this;
    }

    public UITextLabel setTextLeftBold() {
        textLeft.setBold(true);
        return this;
    }

    public UITextLabel setTextCenterBold() {
        textCenter.setBold(true);
        return this;
    }
}
