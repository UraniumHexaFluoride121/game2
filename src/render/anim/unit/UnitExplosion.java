package render.anim.unit;

import foundation.math.ObjPos;
import level.Level;
import render.GameRenderer;
import render.UIColourTheme;
import render.anim.sequence.AnimSequence;
import render.anim.sequence.AnimValue;
import render.anim.sequence.ImageSequenceAnim;
import render.anim.sequence.KeyframeFunction;
import render.anim.timer.LerpAnimation;
import render.particle.Particle;
import render.particle.ParticleBehaviour;
import render.particle.ParticleEmitter;
import render.texture.ImageSequenceGroup;

import java.awt.*;

import static level.tile.Tile.*;

public class UnitExplosion implements Animation {
    private static final float TIME = .5f;
    private final ImageSequenceAnim explosion = new ImageSequenceAnim(ImageSequenceGroup.EXPLOSION.getRandomSequence(), TILE_SIZE * 2f, TIME);
    private final ObjPos center;
    private final ParticleEmitter emitter = new ParticleEmitter();

    public UnitExplosion(ObjPos center, Level level) {
        this.center = center;
        if (level != null) {
            level.levelRenderer.registerTimerBlock(new LerpAnimation(TIME / 2), () -> level.levelRenderer.disableCameraShake());
        }
        emitter.addParticle(new Particle(TIME,
                        ParticleBehaviour.lerpRadialGradient(5, 0.7f, 1,
                                new Color(255, 255, 255, 0), new Color(251, 197, 134),
                                new Color(142, 142, 142, 0), new Color(168, 126, 78, 0),
                                AnimValue.UNCHANGED),
                        ParticleBehaviour.scale(new AnimSequence()
                                .addKeyframe(0, 0.3f, KeyframeFunction.pow(0.5f))
                                .endSequence(1, 1.5f)),
                        ParticleBehaviour.circle(5)
                )
        );
        for (int i = 0; i < 15; i++) {
            emitter.addParticle(
                    new Particle(TIME,
                            ParticleBehaviour.lerpColour(UIColourTheme.lerp(
                                            new Color(243, 207, 166),
                                            new Color(213, 146, 72), (float) Math.random()),
                                    new Color(184, 139, 85, 0), new AnimSequence()
                                            .addKeyframe(0.4f, 0, KeyframeFunction.lerp())
                                            .endSequence(1, 1)),
                            ParticleBehaviour.velocity(new AnimSequence()
                                    .addKeyframe(0, 0.3f * 15, KeyframeFunction.pow(0.5f))
                                    .endSequence(1, 1.5f * 7), Particle.randomAngleOffset(180)),
                            ParticleBehaviour.diamond(0.7f + Particle.randomOffset(0.2f), 0.05f + Particle.randomOffset(0.015f))
                    )
            );
        }
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
                        return 11;
                    }

                    @Override
                    public void tick(float deltaTime) {
                        emitter.tick(deltaTime);
                    }

                    @Override
                    public void render(Graphics2D g) {
                        GameRenderer.renderOffset(center, g, () -> {
                            emitter.render(g);
                            explosion.render(g);
                        });
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
        return explosion.finished();
    }

    @Override
    public float normalisedProgress() {
        return explosion.timer.normalisedProgress();
    }

    @Override
    public float timeElapsed() {
        return explosion.timer.timeElapsed();
    }
}
