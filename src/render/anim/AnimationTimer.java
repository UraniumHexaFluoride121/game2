package render.anim;

public interface AnimationTimer {
    void startTimer();
    boolean finished();
    float normalisedProgress();
}
