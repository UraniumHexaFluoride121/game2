package level.tutorial.sequence.event;

import level.Level;
import unit.action.Action;

public class EventActionComplete extends TutorialEvent {
    public final Action action;

    public EventActionComplete(Level l, Action action) {
        super(l);
        this.action = action;
    }
}
