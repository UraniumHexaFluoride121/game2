package render.particle;

import foundation.Deletable;
import foundation.math.ObjPos;
import foundation.tick.Tickable;
import render.GameRenderer;
import render.Renderable;
import render.UIColourTheme;
import render.anim.sequence.AnimValue;
import render.types.input.button.UIShapeButton;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Supplier;

public class ParticleEmitter implements Renderable, Tickable, Deletable {
    private final ArrayList<Particle> particles = new ArrayList<>();
    private final HashSet<ParticleSupplier> suppliers = new HashSet<>();
    private float timer = 0, x = 0, y = 0;

    public ParticleEmitter() {
    }

    public ParticleEmitter clearSpawners() {
        suppliers.forEach(ParticleSupplier::delete);
        suppliers.clear();
        return this;
    }

    public ParticleEmitter addParticle(Particle p) {
        particles.add(p);
        return this;
    }

    public ParticleEmitter addParticleSupplier(Supplier<Particle> particleSupplier, Supplier<Float> particleRate) {
        suppliers.add(new ParticleSupplier(particleSupplier, particleRate));
        return this;
    }

    public ParticleEmitter setPos(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public ParticleEmitter setPos(ObjPos pos) {
        return setPos(pos.x, pos.y);
    }

    @Override
    public void render(Graphics2D g) {
        particles.removeIf(Particle::finished);
        GameRenderer.renderOffset(x, y, g, () -> {
            particles.forEach(p -> p.render(g));
        });
    }

    @Override
    public void tick(float deltaTime) {
        suppliers.forEach(s -> {
            timer += s.particleRate.get() * deltaTime;
            while (timer >= 1) {
                timer -= 1;
                addParticle(s.particleSupplier.get());
            }
        });
        particles.forEach(p -> p.tick(deltaTime));
    }

    public ParticleEmitter accelerateTime(float time) {
        if (time > 0.1f) {
            accelerateTime(time - 0.1f);
            time = 0.1f;
        }
        float finalTime = time;
        particles.forEach(p -> p.timer.advanceTimer(finalTime));
        particles.removeIf(Particle::finished);
        tick(time);
        return this;
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
        return ParticleBehaviour.onRender((p, g) -> g.fill(shape));
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
        return ParticleBehaviour.onRender((p, g) -> {
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
        return ParticleBehaviour.onRender((p, g) -> g.setColor(c));
    }

    public static ParticleBehaviour lerpColour(Color a, Color b, AnimValue t) {
        return ParticleBehaviour.onRender((p, g) -> g.setColor(UIColourTheme.lerp(a, b, t.getValue(p.progress()))));
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

    public static ParticleBehaviour scale(AnimValue scale) {
        return ParticleBehaviour.onRender((p, g) -> {
            float s = scale.getValue(p.progress());
            g.scale(s, s);
        });
    }

    @Override
    public void delete() {
        clearSpawners();
    }

    private static class ParticleSupplier implements Deletable {
        private Supplier<Particle> particleSupplier;
        private Supplier<Float> particleRate;

        public ParticleSupplier(Supplier<Particle> particleSupplier, Supplier<Float> particleRate) {
            this.particleSupplier = particleSupplier;
            this.particleRate = particleRate;
        }

        @Override
        public void delete() {
            particleSupplier = null;
            particleRate = null;
        }
    }
}
