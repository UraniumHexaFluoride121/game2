package render.anim;

public class SineAnimation implements AnimationTimer {
    private long startTime = System.currentTimeMillis();
    private final float period, initialAngle;

    public SineAnimation(float period, float initialAngle) {
        this.period = period;
        this.initialAngle = (float) Math.toRadians(initialAngle);
    }

    @Override
    public void startTimer() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public boolean finished() {
        return false;
    }

    @Override
    public float normalisedProgress() {
        return (float) Math.sin((System.currentTimeMillis() - startTime) / 1000f * Math.PI * 2 / period + initialAngle);
    }
}
