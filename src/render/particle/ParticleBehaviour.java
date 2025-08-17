package render.particle;

import foundation.math.ObjPos;
import render.Renderable;
import render.UIColourTheme;
import render.anim.sequence.AnimValue;
import render.types.input.button.UIShapeButton;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public record ParticleBehaviour(BiConsumer<Particle, Float> tick, BiConsumer<Particle, Graphics2D> render, Consumer<Particle> initialisation) {
    private static final BiConsumer<Particle, Float> emptyTick = (p, dT) -> {
    };
    private static final BiConsumer<Particle, Graphics2D> emptyRender = (p, g) -> {
    };
    private static final Consumer<Particle> emptyInitialisation = p -> {
    };

    public static ParticleBehaviour onTick(BiConsumer<Particle, Float> tick) {
        return new ParticleBehaviour(tick, emptyRender, emptyInitialisation);
    }

    public static ParticleBehaviour onRender(BiConsumer<Particle, Graphics2D> render) {
        return new ParticleBehaviour(emptyTick, render, emptyInitialisation);
    }

    public static ParticleBehaviour onInitialisation(Consumer<Particle> initialisation) {
        return new ParticleBehaviour(emptyTick, emptyRender, initialisation);
    }

    public static ParticleBehaviour ring(AnimValue size, float width) {
        Stroke stroke = Renderable.roundedStroke(width);
        return onRender((p, g) -> {
            float r = size.getValue(p.progress());
            g.setStroke(stroke);
            g.draw(new Ellipse2D.Float(-r / 2, -r / 2, r, r));
        });
    }

    public static ParticleBehaviour circle(float size) {
        return fillShape(new Ellipse2D.Float(-size / 2, -size / 2, size, size));
    }

    public static ParticleBehaviour plus(float size) {
        return fillShape(UIShapeButton.plus(size));
    }

    public static ParticleBehaviour diamond(float sizeX, float sizeY) {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(-sizeX, 0);
        path.lineTo(0, sizeY);
        path.lineTo(sizeX, 0);
        path.lineTo(0, -sizeY);
        path.closePath();
        return fillShape(path);
    }

    public static ParticleBehaviour rectangle(float sizeX, float sizeY) {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(-sizeX, -sizeY);
        path.lineTo(-sizeX, sizeY);
        path.lineTo(sizeX, sizeY);
        path.lineTo(sizeX, -sizeY);
        path.closePath();
        return fillShape(path);
    }

    public static ParticleBehaviour fillShape(Shape shape) {
        return onRender((p, g) -> g.fill(shape));
    }

    public static ParticleBehaviour staticRadialGradient(float diameter, float innerRadius, float outerRadius, Color inner, Color outer) {
        RadialGradientPaint paint = radialPaint(diameter, innerRadius, outerRadius, inner, outer);
        return new ParticleBehaviour(
                (p, dT) -> {
                },
                (p, g) -> g.setPaint(paint),
                p -> {
                }
        );
    }

    public static ParticleBehaviour lerpRadialGradient(float diameter, float innerRadius, float outerRadius, Color innerA, Color outerA, Color innerB, Color outerB, AnimValue t) {
        return onRender((p, g) -> {
                    float value = t.getValue(p.progress());
                    g.setPaint(radialPaint(diameter, innerRadius, outerRadius, UIColourTheme.lerp(innerA, innerB, value), UIColourTheme.lerp(outerA, outerB, value)));
                }
        );
    }

    private static RadialGradientPaint radialPaint(float diameter, float innerRadius, float outerRadius, Color inner, Color outer) {
        RadialGradientPaint paint;
        float radius = diameter / 2;
        if (innerRadius == 0) {
            if (outerRadius == 1) {
                paint = new RadialGradientPaint(0, 0, radius, new float[]{0, 1}, new Color[]{inner, outer}, MultipleGradientPaint.CycleMethod.REFLECT);
            } else {
                paint = new RadialGradientPaint(0, 0, radius, new float[]{0, outerRadius, 1}, new Color[]{inner, outer, outer}, MultipleGradientPaint.CycleMethod.REFLECT);
            }
        } else {
            if (outerRadius == 1) {
                paint = new RadialGradientPaint(0, 0, radius, new float[]{0, innerRadius, 1}, new Color[]{inner, inner, outer}, MultipleGradientPaint.CycleMethod.REFLECT);
            } else {
                paint = new RadialGradientPaint(0, 0, radius, new float[]{0, innerRadius, outerRadius, 1}, new Color[]{inner, inner, outer, outer}, MultipleGradientPaint.CycleMethod.REFLECT);
            }
        }
        return paint;
    }

    public static ParticleBehaviour staticColour(Color c) {
        return onRender((p, g) -> g.setColor(c));
    }

    public static ParticleBehaviour lerpColour(Color a, Color b, AnimValue t) {
        return onRender((p, g) -> g.setColor(UIColourTheme.lerp(a, b, t.getValue(p.progress()))));
    }

    public static ParticleBehaviour velocity(AnimValue velocity, float radians) {
        ObjPos vector = new ObjPos(1).rotate(radians);
        return new ParticleBehaviour(
                (p, dT) -> p.offset.add(vector.copy().multiply(velocity.getValue(p.progress()) * dT)),
                (p, g) -> {
                },
                p -> p.rotateShape(radians)
        );
    }

    public static ParticleBehaviour setPosition(AnimValue x, AnimValue y) {
        return onRender((p, g) -> {
            float v = p.progress();
            p.offset.set(x.getValue(v), y.getValue(v));
        });
    }

    public static ParticleBehaviour scale(AnimValue scale) {
        return onRender((p, g) -> {
            float s = scale.getValue(p.progress());
            g.scale(s, s);
        });
    }
}
