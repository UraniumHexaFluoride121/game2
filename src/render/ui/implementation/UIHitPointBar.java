package render.ui.implementation;

import foundation.math.MathUtil;
import render.GameRenderer;
import render.Renderable;
import render.anim.PowAnimation;
import unit.Unit;

import java.awt.*;

public class UIHitPointBar implements Renderable {
    private final BasicStroke stroke;
    private final float border, width, height, spacing;
    private final int segments;
    private final Color borderColour, background, bar;
    private float fill = 0, fillTo;
    private float rounding = 0;
    private PowAnimation fillAnimation = null;

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

    public UIHitPointBar(float border, float width, float height, float spacing, Unit unit) {
        this.border = border;
        this.width = width;
        this.height = height;
        this.spacing = spacing;
        stroke = Renderable.sharpCornerStroke(border * SCALING);
        this.segments = (int) unit.type.hitPoints;
        this.borderColour = unit.team.uiColour.borderColour;
        this.background = unit.team.uiColour.backgroundColourSelected;
        this.bar = unit.team.uiColour.borderColour;
    }

    public UIHitPointBar setRounding(float rounding) {
        this.rounding = rounding;
        return this;
    }

    public UIHitPointBar setFill(float fill) {
        this.fill = fill;
        return this;
    }

    public UIHitPointBar setFill(float fill, float time, float exponent) {
        this.fill = fillAnimation == null ? this.fill : MathUtil.lerp(this.fill, fillTo, fillAnimation.normalisedProgress());
        fillTo = fill;
        fillAnimation = new PowAnimation(time, exponent);
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        GameRenderer.renderScaled(1f / SCALING, g, () -> {
            g.setColor(background);
            g.fillRoundRect(0, 0, (int) (width * SCALING), (int) (height * SCALING), (int) (rounding * SCALING), (int) (rounding * SCALING));
            g.setColor(borderColour);
            g.setStroke(stroke);
            g.drawRoundRect(0, 0, (int) (width * SCALING), (int) (height * SCALING), (int) (rounding * SCALING), (int) (rounding * SCALING));
            g.setColor(bar);
            if (fillAnimation != null && fillAnimation.finished()) {
                fill = fillTo;
                fillAnimation = null;
            }
            float fill = fillAnimation == null ? this.fill : MathUtil.lerp(this.fill, fillTo, fillAnimation.normalisedProgress());
            int lastSegment = (int) fill;
            float total = width - border - spacing * 2, segmentWidth = (total - (segments - 1) * spacing) / segments;
            for (int i = 0; i < lastSegment; i++) {
                g.fillRoundRect((int) ((border / 2 + spacing + (segmentWidth + spacing) * i) * SCALING), (int) ((border / 2 + spacing) * SCALING), (int) (segmentWidth * SCALING), (int) ((height - border - spacing * 2) * SCALING), (int) ((rounding - spacing - border / 2) * SCALING), (int) ((rounding - spacing - border / 2) * SCALING));
            }
            float lastSegmentFill = fill - lastSegment;
            if (lastSegmentFill > 0) {
                g.fillRoundRect((int) ((border / 2 + spacing + (segmentWidth + spacing) * lastSegment) * SCALING), (int) ((border / 2 + spacing) * SCALING), (int) (segmentWidth * lastSegmentFill * SCALING), (int) ((height - border - spacing * 2) * SCALING), (int) ((rounding - spacing - border / 2) * SCALING), (int) ((rounding - spacing - border / 2) * SCALING));
            }
        });
    }
}
