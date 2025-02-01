package render.anim;

import foundation.math.MathUtil;

public class ExpAnimation implements ReversableAnimationTimer {
    private long startTime, endTime;
    private final long time;
    private boolean reversed = false;
    private final float exponent;

    public ExpAnimation(float seconds, float exponent) {
        this.time = (long) (seconds * 1000);
        this.exponent = exponent;
        startTimer();
    }

    @Override
    public void startTimer() {
        startTime = System.currentTimeMillis();
        endTime = startTime + time;
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
        }
        this.reversed = reversed;
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
        float t = ((float) Math.pow(Math.clamp(MathUtil.normalise(0, endTime - startTime, System.currentTimeMillis() - startTime), 0, 1), exponent));
        if (reversed) {
            return 1 - t;
        } else {
            return t;
        }
    }
}
