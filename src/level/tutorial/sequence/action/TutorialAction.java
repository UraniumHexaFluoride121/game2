package level.tutorial.sequence.action;

public class TutorialAction implements TutorialSequenceElement {
    private Runnable action;

    public TutorialAction(Runnable action) {
        this.action = action;
    }

    @Override
    public void start() {
        action.run();
        TutorialSequenceElement.next();
    }

    @Override
    public void end() {

    }

    @Override
    public void delete() {
        action = null;
    }
}
