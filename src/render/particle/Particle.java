package render.particle;

import foundation.math.MathUtil;
import foundation.math.ObjPos;
import foundation.tick.Tickable;
import render.GameRenderer;
import render.Renderable;
import render.anim.LerpAnimation;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.function.BiConsumer;

public class Particle implements Renderable, Tickable {
    public final LerpAnimation timer;
    public final Shape shape;
    public final ParticleModifier[] modifiers;
    public final float lifetime;
    public ObjPos offset = new ObjPos();
    public AffineTransform rotation = new AffineTransform();

    public Particle(float lifetime, Shape shape, ParticleModifier... modifiers) {
        this.lifetime = lifetime;
        this.shape = shape;
        this.modifiers = modifiers;
        for (ParticleModifier modifier : modifiers) {
            modifier.initialisation().accept(this);
        }
        timer = new LerpAnimation(lifetime);
    }

    @Override
    public void render(Graphics2D g) {
        GameRenderer.renderOffset(offset, g, () -> {
            g.transform(rotation);
            for (ParticleModifier modifier : modifiers) {
                modifier.render().accept(this, g);
            }
            g.fill(shape);
        });
    }

    @Override
    public void tick(float deltaTime) {
        for (ParticleModifier modifier : modifiers) {
            modifier.tick().accept(this, deltaTime);
        }
    }

    public Particle offset(ObjPos pos) {
        offset.add(pos);
        return this;
    }

    public Particle offsetRandomOnLine(ObjPos start, ObjPos end) {
        offset.add(start.lerp(end, (float) Math.random()));
        return this;
    }

    public Particle offsetRandomOnLine(float radians, float maxOffset, float forwardOffset) {
        ObjPos p = new ObjPos(maxOffset).rotate(radians);
        return offsetRandomOnLine(p.copy().multiply(1 + forwardOffset), p.copy().multiply(-1 - forwardOffset));
    }

    public Particle rotateShape(float radians) {
        rotation = new AffineTransform();
        rotation.rotate(radians);
        return this;
    }

    public float progress() {
        return timer.normalisedProgress();
    }

    public boolean finished() {
        return timer.finished();
    }

    public static float randomOffset(float amount) {
        return MathUtil.randFloatBetween(-amount, amount, Math::random);
    }

    public static float randomAngleOffset(float degrees) {
        return randomOffset((float) Math.toRadians(degrees));
    }
}
