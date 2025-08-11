package render.particle;

import foundation.math.MathUtil;
import foundation.math.ObjPos;
import foundation.tick.Tickable;
import render.GameRenderer;
import render.Renderable;
import render.anim.timer.LerpAnimation;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Particle implements Renderable, Tickable {
    public final LerpAnimation timer;
    public final ParticleBehaviour[] modifiers;
    public final float lifetime;
    public ObjPos offset = new ObjPos();
    public AffineTransform rotation = new AffineTransform();

    public Particle(float lifetime, ParticleBehaviour... modifiers) {
        this.lifetime = lifetime;
        this.modifiers = modifiers;
        for (ParticleBehaviour behaviour : modifiers) {
            behaviour.initialisation().accept(this);
        }
        timer = new LerpAnimation(lifetime);
    }

    @Override
    public void render(Graphics2D g) {
        GameRenderer.renderOffset(offset, g, () -> {
            g.transform(rotation);
            for (ParticleBehaviour behaviour : modifiers) {
                behaviour.render().accept(this, g);
            }
        });
    }

    @Override
    public void tick(float deltaTime) {
        for (ParticleBehaviour behaviour : modifiers) {
            behaviour.tick().accept(this, deltaTime);
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

    public Particle offsetRandomInCircle(float radius) {
        offset.add(randomRadialOffset(radius));
        return this;
    }

    public static ObjPos randomRadialOffset(float radius) {
        return new ObjPos((float) (radius * Math.random())).rotate(randomAngleOffset(180));
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
