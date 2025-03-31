package level.tutorial.sequence;

import level.tutorial.sequence.action.TutorialSequenceElement;
import level.tutorial.sequence.event.EventConsumer;
import level.tutorial.sequence.event.TutorialEvent;

public class TutorialSequence implements SequenceHolder {
    private TutorialSequenceElement[] sequence = new TutorialSequenceElement[0];
    private int sequenceIndex = 0;

    public void start() {
        sequenceIndex = 0;
        this.sequence[0].start();
    }

    public void setSequence(TutorialSequenceElement[] sequence) {
        this.sequence = sequence;
    }

    @Override
    public boolean incrementSequence() {
        if (getActiveSequenceElement() instanceof SequenceHolder s && !s.incrementSequence())
            return false;
        sequence[sequenceIndex].end();
        sequenceIndex++;
        if (sequenceIndex >= sequence.length)
            return true;
        sequence[sequenceIndex].start();
        return false;
    }

    public void deleteSequence() {
        for (TutorialSequenceElement element : sequence) {
            element.delete();
        }
        sequence = new TutorialSequenceElement[0];
        sequenceIndex = 0;
    }

    public TutorialSequenceElement getActiveSequenceElement() {
        return sequence[sequenceIndex];
    }

    public void acceptEvent(TutorialEvent e) {
        if (getActiveSequenceElement() instanceof EventConsumer c)
            c.accept(e);
    }
}
