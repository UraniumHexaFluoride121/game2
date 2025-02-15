package render.ui.implementation;

import foundation.math.MathUtil;
import render.GameRenderer;
import render.Renderable;
import render.anim.PowAnimation;
import render.ui.UIColourTheme;
import unit.Unit;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class UIHitPointBar implements Renderable {
    private final BasicStroke stroke;
    private final float border, width, height, spacing;
    private final int segments;
    private Color borderColour, background, bar;
    private float fill = 0, fillTo;
    private float rounding = 0;
    private PowAnimation fillAnimation = null;
    private boolean barOnly = false, borderOnly = false;

    public UIHitPointBar(float border, float width, float height, float spacing, int segments, Color borderColour, Color background, Color bar) {
        this.border = border;
        this.width = width;
        this.height = height;
        this.spacing = spacing;
        stroke = Renderable.sharpCornerStroke(border * SCALING);
        this.segments = segments;
        this.borderColour = borderColour;
        this.background = background;
        this.bar = bar;
    }

    public UIHitPointBar(float border, float width, float height, float spacing, int segments, UIColourTheme theme) {
        this.border = border;
        this.width = width;
        this.height = height;
        this.spacing = spacing;
        stroke = Renderable.sharpCornerStroke(border * SCALING);
        this.segments = segments;
        borderColour = theme.borderColour;
        background = theme.backgroundColour;
        bar = theme.borderColour;
    }

    public UIHitPointBar(float border, float width, float height, float spacing, Unit unit) {
        this.border = border;
        this.width = width;
        this.height = height;
        this.spacing = spacing;
        stroke = Renderable.sharpCornerStroke(border * SCALING);
        this.segments = (int) unit.type.hitPoints;
        borderColour = unit.team.uiColour.borderColour;
        background = unit.team.uiColour.backgroundColourSelected;
        bar = unit.team.uiColour.borderColour;
    }

    public UIHitPointBar setBarColour(Color bar) {
        this.bar = bar;
        return this;
    }

    public UIHitPointBar setBorderColour(Color borderColour) {
        this.borderColour = borderColour;
        return this;
    }

    public UIHitPointBar setColour(UIColourTheme theme) {
        borderColour = theme.borderColour;
        background = theme.backgroundColour;
        bar = theme.borderColour;
        return this;
    }

    public UIHitPointBar barOnly() {
        barOnly = true;
        borderOnly = false;
        return this;
    }

    public UIHitPointBar borderOnly() {
        barOnly = false;
        borderOnly = true;
        return this;
    }

    public UIHitPointBar setRounding(float rounding) {
        this.rounding = rounding;
        return this;
    }

    public UIHitPointBar setFill(float fill) {
        this.fill = Math.clamp(fill, 0, segments);
        return this;
    }

    public UIHitPointBar setFill(float fill, float time, float exponent) {
        this.fill = getRenderFill();
        fillTo = Math.clamp(fill, 0, segments);
        fillAnimation = new PowAnimation(time, exponent);
        return this;
    }

    public boolean finished() {
        return fillAnimation == null || fillAnimation.finished();
    }

    public PowAnimation getFillAnimation() {
        return fillAnimation;
    }

    public boolean empty() {
        return fill <= 0;
    }

    public Shape getBarClip() {
        float segmentWidth = getSegmentWidth();
        float fill = getRenderFill();
        RoundRectangle2D.Float rect = new RoundRectangle2D.Float(
                border / 2 + spacing,
                border / 2 + spacing - 0.01f,
                (segmentWidth + spacing) * (int) fill + segmentWidth * (fill - (int) fill),
                height - border - spacing * 2 + 0.02f,
                rounding - spacing - border / 2,
                rounding - spacing - border / 2
        );
        return rect;
    }

    @Override
    public void render(Graphics2D g) {
        GameRenderer.renderScaled(1f / SCALING, g, () -> {
            if (!barOnly) {
                g.setColor(background);
                g.fillRoundRect(0, 0, (int) (width * SCALING), (int) (height * SCALING), (int) (rounding * SCALING), (int) (rounding * SCALING));
                g.setColor(borderColour);
                g.setStroke(stroke);
                g.drawRoundRect(0, 0, (int) (width * SCALING), (int) (height * SCALING), (int) (rounding * SCALING), (int) (rounding * SCALING));
                if (borderOnly)
                    return;
            }
            g.setColor(bar);
            if (fillAnimation != null && fillAnimation.finished()) {
                fill = fillTo;
                fillAnimation = null;
            }
            float fill = getRenderFill();
            int lastSegment = (int) fill;
            float segmentWidth = getSegmentWidth();
            for (int i = 0; i < lastSegment; i++) {
                g.fillRoundRect((int) ((border / 2 + spacing + (segmentWidth + spacing) * i) * SCALING), (int) ((border / 2 + spacing) * SCALING), (int) (segmentWidth * SCALING), (int) ((height - border - spacing * 2) * SCALING), (int) ((rounding - spacing - border / 2) * SCALING), (int) ((rounding - spacing - border / 2) * SCALING));
            }
            float lastSegmentFill = fill - lastSegment;
            if (lastSegmentFill > 0) {
                g.fillRoundRect((int) ((border / 2 + spacing + (segmentWidth + spacing) * lastSegment) * SCALING), (int) ((border / 2 + spacing) * SCALING), (int) (segmentWidth * lastSegmentFill * SCALING), (int) ((height - border - spacing * 2) * SCALING), (int) ((rounding - spacing - border / 2) * SCALING), (int) ((rounding - spacing - border / 2) * SCALING));
            }
        });
    }

    private float getRenderFill() {
        return fillAnimation == null ? this.fill : MathUtil.lerp(this.fill, fillTo, fillAnimation.normalisedProgress());
    }

    private float getTotalBarWidth() {
        return width - border - spacing * 2;
    }

    private float getSegmentWidth() {
        return (getTotalBarWidth() - (segments - 1) * spacing) / segments;
    }
}
