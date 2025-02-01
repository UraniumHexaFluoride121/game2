package render.anim;

public class PowAnimation implements ReversableAnimationTimer {
    private long startTime;
    private final float cutoff;
    private boolean reversed = false;
    private final float exponent;

    public PowAnimation(float cutoff, float exponent) {
        this.cutoff = cutoff;
        this.exponent = exponent;
        startTimer();
    }

    @Override
    public void startTimer() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void setReversed(boolean reversed) {
        if (this.reversed != reversed) {
            float progress = normalisedProgress();
            if (reversed) {
                startTime = System.currentTimeMillis() - (long) ((-Math.log(progress) / exponent) * 1000f);
            } else
                startTime = System.currentTimeMillis() - (long) ((-Math.log(1 - progress) / exponent) * 1000f);
        }
        this.reversed = reversed;
    }

    @Override
    public boolean reversed() {
        return reversed;
    }

    @Override
    public boolean finished() {
        return normalisedProgress() >= cutoff;
    }

    @Override
    public float normalisedProgress() {
        float exp = (float) Math.exp(-exponent * (System.currentTimeMillis() - startTime) / 1000f);
        if (reversed)
            return exp;
        return 1 - exp;
    }
}