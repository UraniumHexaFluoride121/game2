package render.anim;

import network.Writable;

public interface AnimationTimer extends Writable {
    void startTimer();
    boolean finished();
    float normalisedProgress();
}
