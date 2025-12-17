package level.tutorial.sequence.action;

import level.tutorial.TutorialManager;
import level.tutorial.sequence.SequenceHolder;
import level.tutorial.sequence.TutorialSequence;
import level.tutorial.sequence.event.EventConsumer;
import level.tutorial.sequence.event.TutorialEvent;
import level.tutorial.sequence.event.TutorialEventListener;

public class BranchListener implements EventConsumer, SequenceHolder {
    private final TutorialSequence tutorialSequence = new TutorialSequence();
    private TutorialEventListener trigger, skip;
    private boolean sequence = false;

    public static BranchListener waitFor(TutorialEventListener trigger, TutorialEventListener skip, TutorialSequenceElement sequence) {
        return new BranchListener(trigger, skip, sequence);
    }

    private BranchListener(TutorialEventListener trigger, TutorialEventListener skip, TutorialSequenceElement... sequence) {
        this.trigger = trigger;
        this.skip = skip;
        tutorialSequence.setSequence(sequence);
    }

    @Override
    public TutorialEventListener[] getListeners() {
        return new TutorialEventListener[]{trigger, skip};
    }

    @Override
    public void end() {

    }

    @Override
    public void delete() {
        tutorialSequence.deleteSequence();
        trigger = null;
        skip = null;
    }

    @Override
    public void accept(TutorialEvent e) {
        if (sequence) {
            tutorialSequence.acceptEvent(e);
            return;
        }
        if (trigger.test(e)) {
            sequence = true;
            tutorialSequence.start();
        } else if (skip.test(e)) {
            TutorialManager.incrementSequence();
        }
    }

    @Override
    public boolean incrementSequence() {
        return !sequence || tutorialSequence.incrementSequence();
    }
}
