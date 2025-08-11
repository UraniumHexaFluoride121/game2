package render.anim.unit;

import foundation.Deletable;
import render.anim.timer.AnimationTimer;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Animation extends Deletable, AnimationTimer {
    AnimRenderable[] getElements();

    @Override
    default void startTimer() {
        throw new UnsupportedOperationException();
    }

    @Override
    default void write(DataOutputStream w) throws IOException {
        throw new UnsupportedOperationException();
    }
}
