package render.anim.sequence;

import foundation.math.MathUtil;

public final class LerpValue implements AnimValue {
    private final float startTime, endTime, startValue, endValue;
    private boolean clamp = false;

    public LerpValue(float startTime, float endTime, float startValue, float endValue) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public LerpValue clamp() {
        clamp = true;
        return this;
    }

    @Override
    public float getValue(float time) {
        return MathUtil.map(startTime, endTime, startValue, endValue, time, clamp);
    }
}
