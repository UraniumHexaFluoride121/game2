package render.anim.unit;

import foundation.TimedTaskQueue;
import foundation.math.ObjPos;
import foundation.math.RandomHandler;
import render.GameRenderer;
import render.anim.sequence.*;
import render.anim.timer.LerpAnimation;
import render.particle.Particle;
import render.particle.ParticleBehaviour;
import render.particle.ParticleEmitter;
import render.texture.ImageSequenceGroup;

import java.awt.*;

import static level.tile.Tile.*;

public class ObjectExplosion implements Animation {
    private final ObjPos center;
    private final IndexedImageSequence[] anims;
    private final LerpAnimation fullAnim;
    private final Color explosionParticleColour = new Color(216, 144, 101, 255);
    private final Color explosionParticleColour2 = new Color(145, 92, 55, 255);
    private final Color shieldedExplosionParticleColour = new Color(116, 186, 211, 255);
    private final Color shieldedExplosionParticleColour2 = new Color(55, 118, 145, 255);

    private final TimedTaskQueue animTasks = new TimedTaskQueue();

    private final ParticleEmitter explosionParticleEmitter = new ParticleEmitter();

    private final boolean shieldParticles;
    private final AnimType animType;

    public ObjectExplosion(ObjPos center, float time, boolean shieldParticles, AnimType animType) {
        this.shieldParticles = shieldParticles;
        this.animType = animType;
        this.center = center;
        fullAnim = new LerpAnimation(time);
        time /= 2.6f;
        Integer[] indices = RandomHandler.randomise(new Integer[]{0, 1, 2}, Math::random);
        anims = new IndexedImageSequence[]{
                new IndexedImageSequence(new ImageSequenceAnim(ImageSequenceGroup.EXPLOSION.getRandomSequence(), TILE_SIZE, time), indices[0]),
                new IndexedImageSequence(new ImageSequenceAnim(ImageSequenceGroup.EXPLOSION.getRandomSequence(), TILE_SIZE, time, time * 0.8f), indices[1]),
                new IndexedImageSequence(new ImageSequenceAnim(ImageSequenceGroup.EXPLOSION.getRandomSequence(), TILE_SIZE, time, time * 1.6f), indices[2])
        };
        explosionParticles(anims[0].index);
        animTasks.addTask(time * 0.8f, () -> explosionParticles(anims[1].index));
        animTasks.addTask(time * 1.6f, () -> explosionParticles(anims[2].index));
        explosionParticleEmitter.setPos(center);
    }

    @Override
    public boolean finished() {
        return fullAnim.finished();
    }

    @Override
    public float normalisedProgress() {
        return fullAnim.normalisedProgress();
    }

    @Override
    public float timeElapsed() {
        return fullAnim.timeElapsed();
    }

    private static final ParticleBehaviour circle = ParticleEmitter.circle(0.3f);

    private void explosionParticles(int index) {
        ObjPos pos = new ObjPos(xOffset(index), yOffset(index));
        for (int i = 0; i < 20; i++) {
            explosionParticleEmitter.addParticle(new Particle(0.3f,
                            ParticleEmitter.lerpColour(shieldParticles ? shieldedExplosionParticleColour : explosionParticleColour, shieldParticles ? shieldedExplosionParticleColour2 : explosionParticleColour2, AnimValue.UNCHANGED),
                            ParticleEmitter.velocity(new LerpValue(0, 1, 10 + Particle.randomOffset(4), 1), Particle.randomAngleOffset(180)),
                            ParticleEmitter.scale(new AnimSequence()
                                    .addKeyframe(0, 1, KeyframeFunction.lerp())
                                    .addKeyframe(0.2f, 1, KeyframeFunction.lerp())
                                    .endSequence(1, 0f).andThen(AnimValue.scaledValue(1 + Particle.randomOffset(0.4f)))),
                            circle
                    ).offset(pos)
            );
        }
    }

    @Override
    public AnimRenderable[] getElements() {
        return new AnimRenderable[]{
                new AnimRenderable() {
                    @Override
                    public AnimType type() {
                        return animType;
                    }

                    @Override
                    public int zOrder() {
                        return 10;
                    }

                    @Override
                    public void tick(float deltaTime) {
                        explosionParticleEmitter.tick(deltaTime);
                        animTasks.tick(deltaTime);
                    }

                    @Override
                    public void render(Graphics2D g) {
                        GameRenderer.renderOffset(center, g, () -> {
                            for (IndexedImageSequence anim : anims) {
                                GameRenderer.renderOffset(xOffset(anim.index), yOffset(anim.index), g, () -> {
                                    anim.anim.render(g);
                                });
                            }
                        });
                        explosionParticleEmitter.render(g);
                    }
                }
        };
    }

    private float xOffset(int index) {
        return switch (index) {
            case 0 -> TILE_SIZE * 0.1f;
            case 1 -> 0;
            case 2 -> TILE_SIZE * -0.1f;
            default -> throw new RuntimeException();
        };
    }

    private float yOffset(int index) {
        return switch (index) {
            case 0 -> TILE_SIZE * -0.15f;
            case 1 -> TILE_SIZE * 0.15f;
            case 2 -> TILE_SIZE * -0.05f;
            default -> throw new RuntimeException();
        };
    }

    @Override
    public void delete() {
        explosionParticleEmitter.delete();
        animTasks.delete();
    }

    private record IndexedImageSequence(ImageSequenceAnim anim, int index) {

    }
}