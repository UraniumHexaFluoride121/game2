package unit.weapon;

import foundation.math.MathUtil;
import render.GameRenderer;
import render.Renderable;
import render.anim.sequence.ImageSequenceAnim;
import render.anim.timer.LerpAnimation;

import java.awt.*;

public class Projectile implements Renderable {
    public final ProjectileType type;
    private float x, y;
    private LerpAnimation forwardAnim;
    private final LerpAnimation holdAnim = new LerpAnimation(1);
    private boolean end = false, exploding = false;
    private float endDistance = 15;
    private final ImageSequenceAnim explosion, spawnAnim;

    public Projectile(ProjectileType type, float x, float y) {
        this.type = type;
        this.x = x;
        this.y = y;
        forwardAnim = new LerpAnimation((30 - x) / type.velocity);
        explosion = type.hitAnim();
        spawnAnim = type.spawnAnim();
    }

    @Override
    public void render(Graphics2D g) {
        if (spawnAnim != null)
            GameRenderer.renderOffset(x, y, g, () -> {
                spawnAnim.render(g);
            });
        if (!end && holdAnim.finished()) {
            end = true;
            endDistance = MathUtil.randFloatBetween(40, 50, Math::random);
            forwardAnim = new LerpAnimation((endDistance - 31) / type.velocity);
        }
        if (end) {
            Shape clip = g.getClip();
            g.clipRect(30, 0, 30, (int) (Renderable.top() + 1));
            GameRenderer.renderOffset(MathUtil.lerp(31, endDistance, forwardAnim.normalisedProgress()), y, g, () -> {
                if (forwardAnim.finished()) {
                    if (!exploding) {
                        explosion.start();
                        exploding = true;
                    }
                    explosion.render(g);
                } else {
                    type.render(g);
                }
            });
            g.setClip(clip);
        } else {
            if (forwardAnim.finished())
                return;
            Shape clip = g.getClip();
            g.clipRect(0, 0, 30, (int) (Renderable.top() + 1));
            GameRenderer.renderOffset(x + forwardAnim.normalisedProgress() * (30 - x), y, g, () -> {
                type.render(g);
            });
            g.setClip(clip);
        }
    }

    public boolean pastHalfway() {
        return holdAnim.finished();
    }

    public boolean exploding() {
        return exploding;
    }

    public boolean finished() {
        return exploding && explosion.finished();
    }
}
