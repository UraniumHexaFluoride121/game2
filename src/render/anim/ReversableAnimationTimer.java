package render.anim;

public interface ReversableAnimationTimer extends AnimationTimer {
    void setReversed(boolean reversed);
    boolean reversed();
}
