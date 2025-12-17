package level.tutorial.sequence.action;

import level.tutorial.sequence.SequenceHolder;
import level.tutorial.sequence.TutorialSequence;
import level.tutorial.sequence.event.EventConsumer;
import level.tutorial.sequence.event.TutorialEvent;
import level.tutorial.sequence.event.TutorialEventListener;

public class SubSequence implements EventConsumer, SequenceHolder {
    private final TutorialSequence tutorialSequence = new TutorialSequence();

    public static SubSequence sequence(TutorialSequenceElement... sequence) {
        return new SubSequence(sequence);
    }

    private SubSequence(TutorialSequenceElement... sequence) {
        tutorialSequence.setSequence(sequence);
    }

    @Override
    public TutorialEventListener[] getListeners() {
        return new TutorialEventListener[0];
    }

    @Override
    public void start() {
        tutorialSequence.start();
    }

    @Override
    public void end() {
    }

    @Override
    public void delete() {
        tutorialSequence.deleteSequence();
    }

    @Override
    public void accept(TutorialEvent e) {
        tutorialSequence.acceptEvent(e);
    }

    @Override
    public boolean incrementSequence() {
        return tutorialSequence.incrementSequence();
    }
}
