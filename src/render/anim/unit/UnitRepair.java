package render.anim.unit;

import foundation.math.MathUtil;
import foundation.math.ObjPos;
import level.tile.Tile;
import render.UIColourTheme;
import render.anim.sequence.AnimSequence;
import render.anim.sequence.KeyframeFunction;
import render.anim.timer.LerpAnimation;
import render.particle.Particle;
import render.particle.ParticleEmitter;

import java.awt.*;

public class UnitRepair implements Animation {
    private static final float TIME = 1.2f;
    private final LerpAnimation timer = new LerpAnimation(TIME);
    private final ParticleEmitter emitter = new ParticleEmitter();

    public UnitRepair(ObjPos center) {
        emitter.setPos(center);
        emitter.addParticleSupplier(UnitRepair::createParticle, () -> timer.timeElapsed() < 0.2f ? 80 : 0f);
    }

    private static Particle createParticle() {
        ObjPos offset = Particle.randomRadialOffset(Tile.TILE_SIZE / 2);
        float v = (float) Math.sqrt((1 - (offset.y + Tile.TILE_SIZE / 2) / Tile.TILE_SIZE) + 0.2f);
        float sizeOffset = Particle.randomOffset(0.07f);
        return new Particle(TIME - 0.2f,
                ParticleEmitter.scale(new AnimSequence()
                        .addKeyframe(0, 0, KeyframeFunction.pow(0.5f))
                        .addKeyframe(0.4f, 1, KeyframeFunction.lerp())
                        .addKeyframe(0.6f, 1, KeyframeFunction.lerp())
                        .endSequence(1, 0)),
                ParticleEmitter.velocity(new AnimSequence()
                        .addKeyframe(0, 1.5f * v, KeyframeFunction.lerp())
                        .endSequence(0.25f, 2.5f * v), (float) Math.toRadians(90)),
                ParticleEmitter.staticColour(UIColourTheme.lerp(
                        new Color(62, 145, 34, 110),
                        new Color(115, 211, 83, 98), MathUtil.normalise(-0.07f, 0.07f, sizeOffset))),
                ParticleEmitter.plus(0.4f + sizeOffset),
                ParticleEmitter.staticColour(UIColourTheme.lerp(
                        new Color(73, 175, 39, 163),
                        new Color(131, 239, 95, 179), MathUtil.normalise(-0.07f, 0.07f, sizeOffset))),
                ParticleEmitter.plus(0.3f + sizeOffset)
        ).offset(offset);
    }

    @Override
    public AnimRenderable[] getElements() {
        return new AnimRenderable[]{
                new AnimRenderable() {
                    @Override
                    public AnimType type() {
                        return AnimType.ABOVE_UNIT;
                    }

                    @Override
                    public int zOrder() {
                        return 5;
                    }

                    @Override
                    public void tick(float deltaTime) {
                        emitter.tick(deltaTime);
                    }

                    @Override
                    public void render(Graphics2D g) {
                        emitter.render(g);
                    }
                }
        };
    }

    @Override
    public void delete() {
        emitter.delete();
    }

    @Override
    public boolean finished() {
        return timer.finished();
    }

    @Override
    public float normalisedProgress() {
        return timer.normalisedProgress();
    }

    @Override
    public float timeElapsed() {
        return timer.timeElapsed();
    }
}
