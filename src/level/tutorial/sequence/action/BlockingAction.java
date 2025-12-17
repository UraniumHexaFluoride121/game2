package level.tutorial.sequence.action;

import level.tutorial.TutorialManager;
import level.tutorial.sequence.event.EventConsumer;
import level.tutorial.sequence.event.TutorialEvent;
import level.tutorial.sequence.event.TutorialEventListener;

public class BlockingAction implements EventConsumer {
    private TutorialEventListener eventListener;

    public static BlockingAction waitFor(TutorialEventListener eventListener) {
        return new BlockingAction(eventListener);
    }

    private BlockingAction(TutorialEventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    public void accept(TutorialEvent e) {
        if (eventListener.test(e))
            TutorialManager.incrementSequence();
    }

    @Override
    public TutorialEventListener[] getListeners() {
        return new TutorialEventListener[]{eventListener};
    }

    @Override
    public void end() {

    }

    @Override
    public void delete() {
        eventListener.delete();
        eventListener = null;
    }
}
