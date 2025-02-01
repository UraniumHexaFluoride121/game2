package unit.weapon;

import foundation.math.MathUtil;
import render.GameRenderer;
import render.Renderable;
import render.anim.ImageSequenceAnim;
import render.anim.LerpAnimation;
import render.texture.ImageSequence;

import java.awt.*;

public class Projectile implements Renderable {
    private final ProjectileType type;
    private float x, y;
    private LerpAnimation forwardAnim;
    private final LerpAnimation holdAnim = new LerpAnimation(1);
    private boolean end = false, exploding = false;
    private float endDistance = 15;
    private final ImageSequenceAnim explosion;

    public Projectile(ProjectileType type, float x, float y) {
        this.type = type;
        this.x = x;
        this.y = y;
        forwardAnim = new LerpAnimation((30 - x) / type.velocity);
        explosion = type.hitAnim();
    }

    @Override
    public void render(Graphics2D g) {
        if (!end && holdAnim.finished()) {
            end = true;
            endDistance = MathUtil.randFloatBetween(40, 50, Math::random);
            forwardAnim = new LerpAnimation((endDistance - 31) / type.velocity);
        }
        if (end) {
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
        } else {
            if (forwardAnim.finished())
                return;
            GameRenderer.renderOffset(x + forwardAnim.normalisedProgress() * (30 - x), y, g, () -> {
                type.render(g);
            });
        }
    }

    public boolean exploding() {
        return exploding;
    }

    public boolean finished() {
        return exploding && explosion.finished();
    }
}
