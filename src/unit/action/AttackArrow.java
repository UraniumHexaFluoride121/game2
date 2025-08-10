package unit.action;

import foundation.Deletable;
import foundation.math.ObjPos;
import level.tile.Tile;
import render.Renderable;
import render.UIColourTheme;
import render.anim.*;
import render.particle.Particle;
import render.particle.ParticleEmitter;
import render.types.text.TextRenderable;

import java.awt.*;
import java.awt.geom.Path2D;

public class AttackArrow implements Renderable, Deletable {
    public static final float ARROW_TIME = 0.5f;
    private static final AnimSequence particleRate = new AnimSequence()
            .addKeyframe(0, 30, KeyframeFunction.lerp())
            .endSequence(0.2f, 0);
    private static final AnimSequence staticParticleRate = new AnimSequence()
            .addKeyframe(0, 30, KeyframeFunction.pow(1.5f))
            .endSequence(1.2f, 0);
    private static final AnimSequence animHeadLength = new AnimSequence()
            .addKeyframe(1.05f, 1, KeyframeFunction.pow(1.5f))
            .endSequence(1.2f, 0);
    private static final AnimSequence animP1 = new AnimSequence()
            .addKeyframe(0.6f, 0, KeyframeFunction.pow(1.5f))
            .endSequence(1.2f, 0.99f);
    private static final AnimSequence animP2 = new AnimSequence()
            .addKeyframe(0, 0.01f, KeyframeFunction.pow(0.5f))
            .endSequence(0.5f, 1);

    public final LerpAnimation timer;
    private final ParticleEmitter arrowParticleEmitter = new ParticleEmitter(), staticArrowParticleEmitter = new ParticleEmitter();

    private final Stroke stroke = Renderable.roundedStroke(0.4f);
    private final Color arrowColour = new Color(207, 57, 57, 255);
    private final Color particleColour = new Color(236, 125, 125, 255);
    private final Color particleColour2 = UIColourTheme.setAlpha(arrowColour, 0.8f);

    private final ObjPos start, end;

    public AttackArrow(ObjPos start, ObjPos end) {
        this.start = start;
        this.end = end;
        timer = new LerpAnimation(ARROW_TIME);

        float angle = end.angleToPos(start), angleOffset = (float) Math.toRadians(40);
        arrowParticleEmitter.addParticleSupplier(() -> {
            return new Particle(1.1f + Particle.randomOffset(0.3f),
                    ParticleEmitter.diamond(0.3f, 0.15f),
                    ParticleEmitter.lerpColour(particleColour, particleColour2, AnimValue.UNCHANGED),
                    ParticleEmitter.velocity(new LerpValue(0, 1, 1.2f, 0.8f), angle + angleOffset),
                    ParticleEmitter.scale(new LerpValue(0, 1, 1, 0.2f))
            ).offsetRandomOnLine(angle, 0.25f, 0.15f);
        }, () -> particleRate.getValue(timer.timeElapsed())).addParticleSupplier(() -> {
            return new Particle(1.1f + Particle.randomOffset(0.3f),
                    ParticleEmitter.diamond(0.3f, 0.15f),
                    ParticleEmitter.lerpColour(particleColour, particleColour2, AnimValue.UNCHANGED),
                    ParticleEmitter.velocity(new LerpValue(0, 1, 1.2f, 0.8f), angle - angleOffset),
                    ParticleEmitter.scale(new LerpValue(0, 1, 1, 0.2f))
            ).offsetRandomOnLine(angle, 0.25f, 0.15f);
        }, () -> particleRate.getValue(timer.timeElapsed())).accelerateTime(1.5f);
        staticArrowParticleEmitter.addParticleSupplier(() -> {
            ObjPos p1 = start.lerp(end, animP1.getValue(timer.timeElapsed()));
            ObjPos p2 = start.lerp(end, animP2.getValue(timer.timeElapsed()));
            float v = (float) Math.sqrt(p1.distance(p2) / Tile.TILE_SIZE);
            return new Particle(0.7f + Particle.randomOffset(0.3f),
                    ParticleEmitter.diamond(0.3f, 0.15f),
                    ParticleEmitter.lerpColour(particleColour, particleColour2, AnimValue.UNCHANGED),
                    ParticleEmitter.velocity(new LerpValue(0, 1, 1.2f * v, 0.8f * v), (float) (angle + Math.toRadians(180))),
                    ParticleEmitter.scale(new LerpValue(0, 1, 1, 0.2f))
            ).offsetRandomOnLine(p1, p2).offsetRandomOnLine((float) (angle + Math.toRadians(90)), 0.2f, 0);
        }, () -> staticParticleRate.getValue(timer.timeElapsed()));
    }

    @Override
    public void render(Graphics2D g) {
        float time = timer.timeElapsed();
        ObjPos p1 = start.lerp(end, animP1.getValue(time));
        ObjPos p2 = start.lerp(end, animP2.getValue(time));
        Path2D.Float shape = TextRenderable.arrowShape(p1.x, p1.y, p2.x, p2.y, 0, 40);
        g.setStroke(stroke);
        g.setColor(UIColourTheme.setAlpha(arrowColour, animHeadLength.getValue(time)));
        g.draw(shape);
        arrowParticleEmitter.setPos(p2).render(g);
        staticArrowParticleEmitter.render(g);
    }

    @Override
    public void delete() {
        arrowParticleEmitter.delete();
        staticArrowParticleEmitter.delete();
    }
}
