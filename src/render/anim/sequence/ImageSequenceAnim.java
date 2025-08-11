package render.anim.sequence;

import render.Renderable;
import render.anim.timer.LerpAnimation;
import render.texture.CachedImageSequence;

import java.awt.*;

public class ImageSequenceAnim implements Renderable {
    public final LerpAnimation timer;
    private final CachedImageSequence sequence;
    public final float width;
    private boolean renderLastWhenFinished = false;

    public ImageSequenceAnim(CachedImageSequence sequence, float width, float time) {
        this.sequence = sequence;
        this.width = width;
        timer = new LerpAnimation(time);
    }

    public ImageSequenceAnim(CachedImageSequence sequence, float width, float time, float delay) {
        this.sequence = sequence;
        this.width = width;
        timer = new LerpAnimation(time);
        timer.startTimer(delay);
    }

    @Override
    public void render(Graphics2D g) {
        if (timer.finished()) {
            if (renderLastWhenFinished)
                sequence.images[sequence.images.length - 1].render(g, width);
            return;
        }
        sequence.images[(int) Math.min(timer.normalisedProgress() * sequence.frames, sequence.frames - 1)].render(g, width);
    }

    public ImageSequenceAnim renderLastWhenFinished() {
        renderLastWhenFinished = true;
        return this;
    }

    public void start() {
        timer.startTimer();
    }

    public boolean finished() {
        return timer.finished();
    }

    public void finish() {
        timer.finish();
    }
}
