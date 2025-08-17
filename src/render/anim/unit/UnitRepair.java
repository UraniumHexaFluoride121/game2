package render.anim.unit;

import foundation.math.MathUtil;
import foundation.math.ObjPos;
import level.tile.Tile;
import render.UIColourTheme;
import render.anim.sequence.AnimSequence;
import render.anim.sequence.KeyframeFunction;
import render.anim.timer.LerpAnimation;
import render.particle.Particle;
import render.particle.ParticleBehaviour;
import render.particle.ParticleEmitter;

import java.awt.*;

public class UnitRepair implements Animation {
    private static final float TIME = 1.2f;
    private final LerpAnimation timer = new LerpAnimation(TIME);
    private final ParticleEmitter emitter = new ParticleEmitter();

    public UnitRepair(ObjPos center) {
        emitter.setPos(center);
        emitter.addParticleSupplier(UnitRepair::createParticle, () -> timer.timeElapsed() < 0.2f ? 40 : 0f);
    }

    private static Particle createParticle() {
        ObjPos offset = Particle.randomRadialOffset(Tile.TILE_SIZE / 3);
        float v = (float) Math.sqrt((1 - (offset.y + Tile.TILE_SIZE / 2) / Tile.TILE_SIZE) + 0.2f);
        float size = (float) Math.random();
        float sizeOffset = MathUtil.lerp(-0.07f, 0.07f, size);
        float brightness = (float) (size * Math.pow(1 - offset.length() / Tile.TILE_SIZE / 2, 0.7f));
        return new Particle(TIME - 0.2f,
                ParticleBehaviour.scale(new AnimSequence()
                        .addKeyframe(0, 0, KeyframeFunction.pow(0.5f))
                        .addKeyframe(0.4f, 1, KeyframeFunction.lerp())
                        .addKeyframe(0.6f, 1, KeyframeFunction.lerp())
                        .endSequence(1, 0)),
                ParticleBehaviour.velocity(new AnimSequence()
                        .addKeyframe(0, 1.5f * v, KeyframeFunction.lerp())
                        .endSequence(0.25f, 2.5f * v), (float) Math.toRadians(90)),
                ParticleBehaviour.staticColour(UIColourTheme.lerp(
                        new Color(43, 108, 23, 66),
                        new Color(114, 221, 79, 140), brightness)),
                ParticleBehaviour.plus(0.4f + sizeOffset),
                ParticleBehaviour.staticColour(UIColourTheme.lerp(
                        new Color(72, 165, 40, 111),
                        new Color(142, 251, 108, 221), brightness)),
                ParticleBehaviour.plus(0.3f + sizeOffset)
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
