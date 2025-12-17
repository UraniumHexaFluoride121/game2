package level.tutorial.sequence.action;

import level.Level;
import level.tutorial.TutorialManager;
import level.tutorial.sequence.SequenceHolder;
import level.tutorial.sequence.TutorialSequence;
import level.tutorial.sequence.event.EventConsumer;
import level.tutorial.sequence.event.TutorialEvent;
import level.tutorial.sequence.event.TutorialEventListener;

import java.util.function.Predicate;

public class BranchAction implements EventConsumer, SequenceHolder {
    private final TutorialSequence tutorialSequence = new TutorialSequence();
    private Predicate<Level> skip;
    private Level l;
    private boolean sequence = false;

    public static BranchAction ifBranch(Level l, Predicate<Level> skip, TutorialSequenceElement sequence) {
        return new BranchAction(l, skip, sequence);
    }

    public static BranchAction skipIfBranch(Level l, Predicate<Level> skip, TutorialSequenceElement sequence) {
        return ifBranch(l, skip.negate(), sequence);
    }

    private BranchAction(Level l, Predicate<Level> skip, TutorialSequenceElement... sequence) {
        this.l = l;
        this.skip = skip;
        tutorialSequence.setSequence(sequence);
    }

    @Override
    public TutorialEventListener[] getListeners() {
        return new TutorialEventListener[0];
    }

    @Override
    public void end() {

    }

    @Override
    public void start() {
        EventConsumer.super.start();
        sequence = skip.test(l);
        if (sequence)
            tutorialSequence.start();
        else
            TutorialManager.incrementSequence();
    }

    @Override
    public void delete() {
        tutorialSequence.deleteSequence();
        skip = null;
        l = null;
    }

    @Override
    public void accept(TutorialEvent e) {
        if (sequence) {
            tutorialSequence.acceptEvent(e);
        }
    }

    @Override
    public boolean incrementSequence() {
        return !sequence || tutorialSequence.incrementSequence();
    }
}
