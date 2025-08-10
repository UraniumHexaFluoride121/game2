package render.particle;

import foundation.Deletable;
import foundation.math.ObjPos;
import foundation.tick.Tickable;
import render.GameRenderer;
import render.Renderable;
import render.UIColourTheme;
import render.anim.AnimValue;
import render.anim.DeltaTime;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ParticleEmitter implements Renderable, Tickable, Deletable {
    private final HashSet<Particle> particles = new HashSet<>();
    private final HashSet<ParticleSupplier> suppliers = new HashSet<>();
    private float timer = 0, x = 0, y = 0;
    private final DeltaTime dT = new DeltaTime();

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
        float deltaTime = dT.get();
        tick(deltaTime);
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

    public static Shape circle(float size) {
        return new Ellipse2D.Float(-size / 2, -size / 2, size, size);
    }

    public static Shape diamond(float sizeX, float sizeY) {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(-sizeX, 0);
        path.lineTo(0, sizeY);
        path.lineTo(sizeX, 0);
        path.lineTo(0, -sizeY);
        path.closePath();
        return path;
    }

    public static ParticleModifier staticColour(Color c) {
        return new ParticleModifier((p, dT) -> {},
                (p, g) -> g.setColor(c),
                p -> {});
    }

    public static ParticleModifier lerpColour(Color a, Color b, AnimValue t) {
        return new ParticleModifier((p, dT) -> {},
                (p, g) -> g.setColor(UIColourTheme.lerp(a, b, t.getValue(p.progress()))),
                p -> {});
    }

    public static ParticleModifier velocity(AnimValue velocity, float radians) {
        ObjPos vector = new ObjPos(1).rotate(radians);
        return new ParticleModifier((p, dT) -> p.offset.add(vector.copy().multiply(velocity.getValue(p.progress()) * dT)),
                (p, g) -> {},
                p -> p.rotateShape(radians));
    }

    public static ParticleModifier scale(AnimValue scale) {
        return new ParticleModifier((p, dT) -> {},
                (p, g) -> {
                    float s = scale.getValue(p.progress());
                    g.scale(s, s);
                },
                p -> {});
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
