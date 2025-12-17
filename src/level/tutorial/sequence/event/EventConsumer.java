package level.tutorial.sequence.event;

import level.tutorial.sequence.action.TutorialSequenceElement;

import java.util.function.Consumer;

public interface EventConsumer extends Consumer<TutorialEvent>, TutorialSequenceElement {
    TutorialEventListener[] getListeners();

    @Override
    default void start() {
        for (TutorialEventListener listener : getListeners()) {
            listener.init();
        }
    }
}
