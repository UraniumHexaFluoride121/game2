package render.particle;

import foundation.Deletable;
import foundation.math.ObjPos;
import foundation.tick.Tickable;
import render.GameRenderer;
import render.Renderable;

import java.awt.*;
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
