package unit.action;

import foundation.Deletable;
import foundation.MainPanel;
import foundation.TimedTaskQueue;
import foundation.math.ObjPos;
import level.structure.ObjectExplosion;
import level.tile.Tile;
import render.Renderable;
import render.UIColourTheme;
import render.anim.*;
import render.particle.Particle;
import render.particle.ParticleEmitter;
import render.types.text.TextRenderable;
import unit.UnitData;

import java.awt.*;
import java.awt.geom.Path2D;

public class UnitDataFiringAnim implements Renderable, Deletable {
    private static final float EXPLOSION_TIME = 1.2f;
    private final UnitData attackingData, defendingData;
    private ObjectExplosion explosion = null;
    public final LerpAnimation timer;
    private final Color explosionParticleColour = new Color(216, 144, 101, 255);
    private final Color explosionParticleColour2 = new Color(145, 92, 55, 255);
    private final Color shieldedExplosionParticleColour = new Color(116, 211, 211, 255);
    private final Color shieldedExplosionParticleColour2 = new Color(55, 145, 137, 255);

    private final ParticleEmitter explosionParticleEmitter = new ParticleEmitter();
    private final TimedTaskQueue animTasks = new TimedTaskQueue();
    private final DeltaTime deltaTime = new DeltaTime();

    private final AttackArrow arrowAnim;

    public UnitDataFiringAnim(ObjPos start, ObjPos end, UnitData attackingData, UnitData defendingData, Runnable onFinished, boolean arrow) {
        this.attackingData = attackingData;
        this.defendingData = defendingData;
        if (arrow) {
            arrowAnim = new AttackArrow(start, end);
            MainPanel.addTimedTask(arrowAnim.timer, () -> {
                startExplosion(end);
            });
        } else {
            arrowAnim = null;
            startExplosion(end);
        }
        timer = new LerpAnimation((arrow ? AttackArrow.ARROW_TIME : 0) + EXPLOSION_TIME);
        MainPanel.addTimedTask(timer, onFinished);
        explosionParticleEmitter.setPos(end);
    }

    private void startExplosion(ObjPos end) {
        explosion = new ObjectExplosion(end, EXPLOSION_TIME);
        explosionParticles();
        animTasks.addTask(EXPLOSION_TIME / 2.6f * 0.8f, this::explosionParticles);
        animTasks.addTask(EXPLOSION_TIME / 2.6f * 1.6f, this::explosionParticles);
    }

    private void explosionParticles() {
        for (int i = 0; i < 20; i++) {
            explosionParticleEmitter.addParticle(new Particle(0.3f,
                    ParticleEmitter.circle(0.3f + Particle.randomOffset(0.1f)),
                    ParticleEmitter.lerpColour(defendingData.shieldRenderHP > 0 ? shieldedExplosionParticleColour : explosionParticleColour, defendingData.shieldRenderHP > 0 ? shieldedExplosionParticleColour2 : explosionParticleColour2, AnimValue.UNCHANGED),
                    ParticleEmitter.velocity(new LerpValue(0, 1, 10 + Particle.randomOffset(4), 1), Particle.randomAngleOffset(180)),
                    ParticleEmitter.scale(new AnimSequence()
                            .addKeyframe(0, 1, KeyframeFunction.lerp())
                            .addKeyframe(0.2f, 1, KeyframeFunction.lerp())
                            .endSequence(1, 0f))
                    )
            );
        }
    }

    @Override
    public void delete() {
        explosionParticleEmitter.delete();
    }

    @Override
    public void render(Graphics2D g) {
        animTasks.tick(deltaTime.get());
        if (explosion != null)
            explosion.render(g);
        explosionParticleEmitter.render(g);
    }

    public void renderBelowUnits(Graphics2D g) {
        if (arrowAnim != null)
            arrowAnim.render(g);
    }
}