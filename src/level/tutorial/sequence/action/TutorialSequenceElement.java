package level.tutorial.sequence.action;

import foundation.Deletable;
import level.tutorial.TutorialManager;

public interface TutorialSequenceElement extends Deletable {
    void start();
    void end();

    static void next() {
        TutorialManager.incrementSequence();
    }

    @Override
    default void delete() {

    }
}
