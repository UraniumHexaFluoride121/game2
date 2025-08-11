package render.anim.timer;

public interface ReversableAnimationTimer extends AnimationTimer {
    void setReversed(boolean reversed);
    boolean reversed();
}
