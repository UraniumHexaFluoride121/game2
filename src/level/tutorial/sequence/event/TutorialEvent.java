package level.tutorial.sequence.event;

import level.Level;

public abstract class TutorialEvent {
    public final Level l;

    public TutorialEvent(Level l) {
        this.l = l;
    }
}
