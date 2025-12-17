package level.tutorial.sequence.event;

import foundation.Deletable;

import java.util.function.Predicate;

@FunctionalInterface
public interface TutorialEventListener extends Predicate<TutorialEvent>, Deletable {
    default TutorialEventListener and(TutorialEventListener other) {
        return new TutorialEventListener() {
            @Override
            public boolean test(TutorialEvent tutorialEvent) {
                return TutorialEventListener.this.test(tutorialEvent) && other.test(tutorialEvent);
            }

            @Override
            public void delete() {
                TutorialEventListener.super.delete();
                other.delete();
            }
        };
    }

    @Override
    default void delete() {

    }

    default void init() {

    }
}
