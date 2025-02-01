package render.ui.implementation;

import foundation.math.MathUtil;
import render.GameRenderer;
import render.Renderable;
import render.anim.ExpAnimation;
import unit.Unit;

import java.awt.*;

public class UIHitPointBar implements Renderable {
    private final BasicStroke stroke;
    private final float border, width, height, spacing;
    private final int segments;
    private final Color borderColour, background, bar;
    private float fill = 0, fillTo;
    private ExpAnimation fillAnimation = null;

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

    public UIHitPointBar setFill(float fill) {
        this.fill = fill;
        return this;
    }

    public UIHitPointBar setFill(float fill, float time, float exponent) {
        fillTo = fill;
        fillAnimation = new ExpAnimation(time, exponent);
        return this;
    }

    @Override
    public void render(Graphics2D g) {
        GameRenderer.renderScaled(1f / SCALING, g, () -> {
            g.setColor(background);
            g.fillRect(0, 0, (int) (width * SCALING), (int) (height * SCALING));
            g.setColor(borderColour);
            g.setStroke(stroke);
            g.drawRect(0, 0, (int) (width * SCALING), (int) (height * SCALING));
            g.setColor(bar);
            if (fillAnimation != null && fillAnimation.finished()) {
                fill = fillTo;
                fillAnimation = null;
            }
            float fill = fillAnimation == null ? this.fill : MathUtil.lerp(this.fill, fillTo, fillAnimation.normalisedProgress());
            int lastSegment = (int) fill;
            for (int i = 0; i < lastSegment; i++) {
                g.fillRect((int) ((spacing + (width - spacing - border / 2) / segments * i) * SCALING), (int) (spacing * SCALING), (int) (((width - spacing - border / 2) / segments - border) * SCALING), (int) ((height - spacing * 2) * SCALING));
            }
            float lastSegmentFill = fill - lastSegment;
            if (lastSegmentFill > 0) {
                g.fillRect((int) ((spacing + (width - spacing - border / 2) / segments * lastSegment) * SCALING), (int) (spacing * SCALING), (int) (((width - spacing - border / 2) / segments - border) * lastSegmentFill * SCALING), (int) ((height - spacing * 2) * SCALING));
            }
        });
    }
}
