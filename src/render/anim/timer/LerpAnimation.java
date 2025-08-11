package render.anim.timer;

import foundation.math.MathUtil;
import network.PacketWriter;
import render.Renderable;
import render.anim.sequence.AnimationType;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Function;

public class LerpAnimation implements ReversableAnimationTimer {
    private long startTime, endTime;
    private final long time;
    private boolean reversed = false;

    public LerpAnimation(float seconds) {
        this.time = (long) (seconds * 1000);
        startTimer();
    }

    public LerpAnimation(DataInputStream reader) throws IOException {
        startTime = reader.readLong();
        endTime = reader.readLong();
        time = reader.readLong();
        reversed = reader.readBoolean();
    }

    @Override
    public float timeElapsed() {
        return (System.currentTimeMillis() - startTime) / 1000f;
    }

    public void renderOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, normalisedProgress()));
        g.fillRect(0, 0, 60, (int) Renderable.top() + 1);
    }

    public void renderOverlay(Graphics2D g, Function<LerpAnimation, Float> alpha) {
        g.setColor(new Color(0, 0, 0, alpha.apply(this)));
        g.fillRect(0, 0, 60, (int) Renderable.top() + 1);
    }

    @Override
    public void startTimer() {
        startTime = System.currentTimeMillis();
        endTime = startTime + time;
    }

    public void startTimer(float delay) {
        startTime = System.currentTimeMillis() + (long) (delay * 1000);
        endTime = startTime + time;
    }

    public void loopTimer() {
        startTime += time;
        endTime += time;
    }

    public void advanceTimer(float time) {
        long t = (long) (time * 1000);
        startTime -= t;
        endTime -= t;
    }

    @Override
    public void setReversed(boolean reversed) {
        if (this.reversed != reversed) {
            long t = System.currentTimeMillis();
            if (t > endTime) {
                startTime = t;
                endTime = t + time;
            } else {
                startTime = -endTime + 2 * t;
                endTime = startTime + time;
            }
            this.reversed = reversed;
        }
    }

    public LerpAnimation finish() {
        endTime = System.currentTimeMillis();
        startTime = endTime - time;
        return this;
    }

    @Override
    public boolean reversed() {
        return reversed;
    }

    @Override
    public boolean finished() {
        return System.currentTimeMillis() > endTime;
    }

    @Override
    public float normalisedProgress() {
        float t = Math.clamp(MathUtil.normalise(0, endTime - startTime, System.currentTimeMillis() - startTime), 0, 1);
        if (reversed) {
            return 1 - t;
        } else {
            return t;
        }
    }

    public float triangleProgress() {
        return 1 - Math.abs(normalisedProgress() * 2 - 1);
    }

    public float doubleTriangleProgress() {
        return 1 - Math.abs(triangleProgress() * 2 - 1);
    }

    @Override
    public void write(DataOutputStream w) throws IOException {
        PacketWriter.writeEnum(AnimationType.LERP, w);
        w.writeLong(startTime);
        w.writeLong(endTime);
        w.writeLong(time);
        w.writeBoolean(reversed);
    }
}
